package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.ChatDTO;
import com.kikisito.salus.api.dto.ChatMessageDTO;
import com.kikisito.salus.api.dto.request.ChatMessageRequest;
import com.kikisito.salus.api.entity.ChatEntity;
import com.kikisito.salus.api.entity.ChatMessagesEntity;
import com.kikisito.salus.api.entity.MedicalProfileEntity;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.exception.ConflictException;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.ChatMessageRepository;
import com.kikisito.salus.api.repository.ChatRepository;
import com.kikisito.salus.api.repository.MedicalProfileRepository;
import com.kikisito.salus.api.repository.UserRepository;
import com.kikisito.salus.api.type.MessageSenderType;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.event.ChangeEvent;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {
    @Autowired
    private final ChatRepository chatRepository;

    @Autowired
    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final MedicalProfileRepository medicalProfileRepository;

    @Autowired
    private final WebSocketService webSocketService;

    @Autowired
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<ChatDTO> getPatientChats(Integer patientId) {
        UserEntity patient = userRepository.findById(patientId).orElseThrow(DataNotFoundException::userNotFound);

        List<ChatEntity> chats = chatRepository.findByPatientOrderByUpdatedAtDesc(patient);

        return chats.stream()
                .map(chat -> this.convertToFullDTO(chat, MessageSenderType.PATIENT))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatDTO> getDoctorChats(Integer doctorId) {
        MedicalProfileEntity doctor = medicalProfileRepository.findById(doctorId).orElseThrow(DataNotFoundException::doctorNotFound);

        List<ChatEntity> chats = chatRepository.findByDoctorOrderByUpdatedAtDesc(doctor);

        return chats.stream()
                .map(chat -> this.convertToFullDTO(chat, MessageSenderType.DOCTOR))
                .toList();
    }

    @Transactional
    public ChatDTO getChatInfo(Integer doctorId, Integer userId, UserEntity userRequest) {
        UserEntity patient = userRepository.findById(userId).orElseThrow(DataNotFoundException::userNotFound);
        MedicalProfileEntity doctor = medicalProfileRepository.findById(doctorId).orElseThrow(DataNotFoundException::doctorNotFound);

        MessageSenderType senderType = userRequest.getId().equals(doctor.getUser().getId()) ? MessageSenderType.DOCTOR : MessageSenderType.PATIENT;

        ChatEntity chat;
        if(senderType == MessageSenderType.PATIENT) {
            chat = chatRepository.findByPatientAndDoctor(patient, doctor).orElseThrow(DataNotFoundException::chatNotFound);
        } else {
            chat = this.getOrCreateConversation(patient, doctor);
        }

        return this.convertToFullDTO(chat, senderType);
    }

    @Transactional
    public List<ChatMessageDTO> getChatMessages(Integer doctorId, Integer userId, UserEntity userRequest) {
        UserEntity patient = userRepository.findById(userId).orElseThrow(DataNotFoundException::userNotFound);
        MedicalProfileEntity doctor = medicalProfileRepository.findById(doctorId).orElseThrow(DataNotFoundException::doctorNotFound);

        ChatEntity chat = chatRepository.findByPatientAndDoctor(patient, doctor).orElseThrow(DataNotFoundException::chatNotFound);

        // El senderType será contrario por que será el tipo que se utiliza para marcar los mensajes como leídos
        MessageSenderType senderType = userRequest.getId().equals(chat.getPatient().getId()) ? MessageSenderType.DOCTOR : userRequest.getId().equals(chat.getDoctor().getId()) ? MessageSenderType.PATIENT : null;

        if(senderType != null) {
            this.markMessagesAsRead(chat, senderType);
        } else {
            throw new SecurityException("User does not have access to this chat");
        }

        List<ChatMessagesEntity> messages = chatMessageRepository.findByChatOrderByCreatedAtAsc(chat);

        return messages.stream()
                .map(message -> modelMapper.map(message, ChatMessageDTO.class))
                .toList();
    }

    @Transactional
    public ChatMessageDTO sendMessage(Integer doctorId, Integer patientId, ChatMessageRequest request, UserEntity sender) {
        // Determinamos si el remitente es el doctor o el paciente
        MessageSenderType senderType = sender.getId().equals(doctorId) ? MessageSenderType.DOCTOR :
                sender.getId().equals(patientId) ? MessageSenderType.PATIENT : null;

        if (senderType == null) {
            throw new AccessDeniedException("Sender is neither the doctor nor the patient");
        }

        // Obtenemos los participantes del chat
        MedicalProfileEntity doctor = medicalProfileRepository.findById(doctorId)
                .orElseThrow(DataNotFoundException::doctorNotFound);
        UserEntity patient = userRepository.findById(patientId)
                .orElseThrow(DataNotFoundException::userNotFound);

        // El médico puede obtener o crear un chat, pero el paciente solo puede obtenerlo
        ChatEntity chat;
        if(senderType == MessageSenderType.DOCTOR) {
            chat = this.getOrCreateConversation(patient, doctor);
        } else {
            chat = chatRepository.findByPatientAndDoctor(patient, doctor).orElseThrow(DataNotFoundException::chatNotFound);
        }

        // Creamos el mensaje
        ChatMessagesEntity message = this.createMessage(chat, request.getMessage(), senderType);

        // Mapear a DTO
        ChatMessageDTO messageDTO = modelMapper.map(message, ChatMessageDTO.class);

        // Enviar por WebSocket al destinatario
        UserEntity recipient = senderType == MessageSenderType.DOCTOR ? patient : doctor.getUser();
        webSocketService.sendMessageToUser(recipient, messageDTO);

        return messageDTO;
    }

    @Transactional
    public void markMessagesAsRead(ChatEntity chat, MessageSenderType senderType) {
        List<ChatMessagesEntity> messages = chatMessageRepository.findByChatAndSenderTypeAndReadIsFalse(chat, senderType);

        for (ChatMessagesEntity message : messages) {
            message.setRead(true);
            chatMessageRepository.save(message);
        }
    }

    private ChatEntity getOrCreateConversation(UserEntity patient, MedicalProfileEntity doctor) {
        // Comprobamos que el paciente y el médico no sean la misma persona
        if(patient.getId().equals(doctor.getUser().getId())) {
            throw ConflictException.cannotCreateChatWithSameSenderAndReceiver();
        }

        Optional<ChatEntity> chat = chatRepository.findByPatientAndDoctor(patient, doctor);

        // Si el chat ya existe, lo devolvemos
        if(chat.isPresent()) {
            return chat.get();
        }

        // Si no existe, lo creamos
        ChatEntity newChat = ChatEntity.builder()
                .patient(patient)
                .doctor(doctor)
                .build();

        return chatRepository.save(newChat);
    }

    private ChatMessagesEntity createMessage(ChatEntity chat, String content, MessageSenderType senderType) {
        ChatMessagesEntity message = ChatMessagesEntity.builder()
                .chat(chat)
                .content(content)
                .senderType(senderType)
                .build();

        return chatMessageRepository.save(message);
    }

    private ChatDTO convertToFullDTO(ChatEntity chat, MessageSenderType recipientType) {
        ChatDTO dto = modelMapper.map(chat, ChatDTO.class);

        // Último mensaje
        List<ChatMessagesEntity> messages = chatMessageRepository.findByChatOrderByCreatedAtAsc(chat);
        if(!messages.isEmpty()) {
            ChatMessagesEntity lastMessage = messages.getLast();
            dto.setLastMessage(modelMapper.map(lastMessage, ChatMessageDTO.class));
        }

        // Número de mensajes no leídos
        int unreadCount = chatMessageRepository.countByChatAndReadFalseAndSenderType(chat, recipientType);
        dto.setUnreadMessages(unreadCount);

        return dto;
    }
}