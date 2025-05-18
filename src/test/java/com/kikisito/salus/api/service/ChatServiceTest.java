package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.ChatDTO;
import com.kikisito.salus.api.dto.ChatMessageDTO;
import com.kikisito.salus.api.dto.request.ChatMessageRequest;
import com.kikisito.salus.api.entity.ChatEntity;
import com.kikisito.salus.api.entity.ChatMessagesEntity;
import com.kikisito.salus.api.entity.MedicalProfileEntity;
import com.kikisito.salus.api.entity.UserEntity;
import com.kikisito.salus.api.exception.DataNotFoundException;
import com.kikisito.salus.api.repository.ChatMessageRepository;
import com.kikisito.salus.api.repository.ChatRepository;
import com.kikisito.salus.api.repository.MedicalProfileRepository;
import com.kikisito.salus.api.repository.UserRepository;
import com.kikisito.salus.api.type.MessageSenderType;
import com.kikisito.salus.api.type.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChatServiceTest {

    @MockitoBean
    private ChatRepository chatRepository;

    @MockitoBean
    private ChatMessageRepository chatMessageRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private MedicalProfileRepository medicalProfileRepository;

    @MockitoBean
    private WebSocketService webSocketService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ChatService chatService;

    private UserEntity testPatient;
    private UserEntity testDoctorUser;
    private MedicalProfileEntity testDoctor;
    private ChatEntity testChat;
    private ChatMessagesEntity testChatMessage;
    private ChatDTO testChatDTO;
    private ChatMessageDTO testChatMessageDTO;
    private List<ChatEntity> testChats;
    private List<ChatMessagesEntity> testMessages;

    @BeforeEach
    void setUp() {
        // Paciente de prueba
        testPatient = UserEntity.builder()
                .id(1)
                .nombre("Juan")
                .apellidos("García")
                .email("juang@salus.com")
                .nif("12345678A")
                .rolesList(new ArrayList<>(List.of(RoleType.USER)))
                .build();

        // Profesional de prueba
        testDoctorUser = UserEntity.builder()
                .id(2)
                .nombre("María")
                .apellidos("Guerrero")
                .email("mariag@salus.com")
                .nif("87654321B")
                .rolesList(new ArrayList<>(List.of(RoleType.USER, RoleType.PROFESSIONAL)))
                .build();

        // Perfil médico del profesional
        testDoctor = MedicalProfileEntity.builder()
                .id(1)
                .user(testDoctorUser)
                .license("COLEG-TEST-1")
                .specialties(new ArrayList<>())
                .build();

        // Chat entity
        testChat = ChatEntity.builder()
                .id(1)
                .doctor(testDoctor)
                .patient(testPatient)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Mensajes
        testChatMessage = ChatMessagesEntity.builder()
                .id(1)
                .chat(testChat)
                .content("Hola, ¿cómo puedo ayudarle?")
                .senderType(MessageSenderType.DOCTOR)
                .read(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testChatMessageDTO = modelMapper.map(testChatMessage, ChatMessageDTO.class);
        testChatDTO = modelMapper.map(testChat, ChatDTO.class);
        testChatDTO.setLastMessage(testChatMessageDTO);
        testChatDTO.setUnreadMessages(1);
        testChats = new ArrayList<>(Collections.singletonList(testChat));
        testMessages = new ArrayList<>(Collections.singletonList(testChatMessage));
    }

    @Test
    void C1_getPatientChats_should_return_one_chat() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(chatRepository.findByPatientOrderByUpdatedAtDesc(testPatient)).thenReturn(testChats);
        when(chatMessageRepository.findByChatOrderByCreatedAtAsc(testChat)).thenReturn(testMessages);
        when(chatMessageRepository.countByChatAndReadFalseAndSenderType(any(), any())).thenReturn(1);

        // Act
        List<ChatDTO> result = assertDoesNotThrow(() -> chatService.getPatientChats(1));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testChatDTO.getId(), result.getFirst().getId());
            assertEquals(testChatDTO.getDoctor().getId(), result.getFirst().getDoctor().getId());
            assertEquals(testChatDTO.getPatient().getId(), result.getFirst().getPatient().getId());
            assertEquals(testChatDTO.getLastMessage().getContent(), result.getFirst().getLastMessage().getContent());
            assertEquals(testChatDTO.getUnreadMessages(), result.getFirst().getUnreadMessages());
        });

        verify(userRepository).findById(1);
        verify(chatRepository).findByPatientOrderByUpdatedAtDesc(testPatient);
        verify(chatMessageRepository).findByChatOrderByCreatedAtAsc(testChat);
        verify(chatMessageRepository).countByChatAndReadFalseAndSenderType(any(), any());
    }

    @Test
    void C2_getDoctorChats_should_return_one_chat() {
        // Arrange
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(chatRepository.findByDoctorOrderByUpdatedAtDesc(testDoctor)).thenReturn(testChats);
        when(chatMessageRepository.findByChatOrderByCreatedAtAsc(testChat)).thenReturn(testMessages);
        when(chatMessageRepository.countByChatAndReadFalseAndSenderType(any(), any())).thenReturn(1);

        // Act
        List<ChatDTO> result = assertDoesNotThrow(() -> chatService.getDoctorChats(1));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testChatDTO.getId(), result.getFirst().getId());
            assertEquals(testChatDTO.getDoctor().getId(), result.getFirst().getDoctor().getId());
            assertEquals(testChatDTO.getPatient().getId(), result.getFirst().getPatient().getId());
            assertEquals(testChatDTO.getLastMessage().getContent(), result.getFirst().getLastMessage().getContent());
            assertEquals(testChatDTO.getUnreadMessages(), result.getFirst().getUnreadMessages());
        });

        verify(medicalProfileRepository).findById(1);
        verify(chatRepository).findByDoctorOrderByUpdatedAtDesc(testDoctor);
        verify(chatMessageRepository).findByChatOrderByCreatedAtAsc(testChat);
        verify(chatMessageRepository).countByChatAndReadFalseAndSenderType(any(), any());
    }

    @Test
    void C3_getChatInfo_should_return_chat_when_patient_requests() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(chatRepository.findByPatientAndDoctor(testPatient, testDoctor)).thenReturn(Optional.of(testChat));
        when(chatMessageRepository.findByChatOrderByCreatedAtAsc(testChat)).thenReturn(testMessages);
        when(chatMessageRepository.countByChatAndReadFalseAndSenderType(any(), any())).thenReturn(1);

        // Act
        ChatDTO result = assertDoesNotThrow(() -> chatService.getChatInfo(1, 1, testPatient));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(testChatDTO.getId(), result.getId());
            assertEquals(testChatDTO.getDoctor().getId(), result.getDoctor().getId());
            assertEquals(testChatDTO.getPatient().getId(), result.getPatient().getId());
            assertEquals(testChatDTO.getLastMessage().getContent(), result.getLastMessage().getContent());
            assertEquals(testChatDTO.getUnreadMessages(), result.getUnreadMessages());
        });

        verify(userRepository).findById(1);
        verify(medicalProfileRepository).findById(1);
        verify(chatRepository).findByPatientAndDoctor(testPatient, testDoctor);
        verify(chatMessageRepository).findByChatOrderByCreatedAtAsc(testChat);
        verify(chatMessageRepository).countByChatAndReadFalseAndSenderType(any(), any());
    }

    @Test
    void C4_getChatInfo_should_create_chat_when_doctor_requests_and_chat_does_not_exist() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(chatRepository.findByPatientAndDoctor(testPatient, testDoctor)).thenReturn(Optional.empty());
        when(chatRepository.save(any(ChatEntity.class))).thenReturn(testChat);
        when(chatMessageRepository.findByChatOrderByCreatedAtAsc(testChat)).thenReturn(Collections.emptyList());
        when(chatMessageRepository.countByChatAndReadFalseAndSenderType(any(), any())).thenReturn(0);

        // Act
        ChatDTO result = assertDoesNotThrow(() -> chatService.getChatInfo(1, 1, testDoctorUser));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(testChat.getId(), result.getId());
            assertEquals(testChat.getDoctor().getId(), result.getDoctor().getId());
            assertEquals(testChat.getPatient().getId(), result.getPatient().getId());
            assertNull(result.getLastMessage());
            assertEquals(0, result.getUnreadMessages());
        });

        verify(userRepository).findById(1);
        verify(medicalProfileRepository).findById(1);
        verify(chatRepository).findByPatientAndDoctor(testPatient, testDoctor);
        verify(chatRepository).save(any(ChatEntity.class));
        verify(chatMessageRepository).findByChatOrderByCreatedAtAsc(testChat);
        verify(chatMessageRepository).countByChatAndReadFalseAndSenderType(any(), any());
    }

    @Test
    void C5_getChatMessages_should_return_chat_messages_and_mark_as_read() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(chatRepository.findByPatientAndDoctor(testPatient, testDoctor)).thenReturn(Optional.of(testChat));
        when(chatMessageRepository.findByChatOrderByCreatedAtAsc(testChat)).thenReturn(testMessages);
        when(chatMessageRepository.findByChatAndSenderTypeAndReadIsFalse(testChat, MessageSenderType.DOCTOR)).thenAnswer(
                invocation -> {
                    List<ChatMessagesEntity> unreadMessages = new ArrayList<>(Collections.singletonList(testChatMessage));
                    unreadMessages.forEach(message -> message.setRead(true));
                    return unreadMessages;
                }
        );

        // Act
        List<ChatMessageDTO> result = assertDoesNotThrow(() -> chatService.getChatMessages(1, 1, testPatient));

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testChatMessageDTO.getId(), result.getFirst().getId());
            assertEquals(testChatMessageDTO.getContent(), result.getFirst().getContent());
            assertEquals(testChatMessageDTO.getSenderType(), result.getFirst().getSenderType());
            assertTrue(result.getFirst().getRead());
        });

        verify(userRepository).findById(1);
        verify(medicalProfileRepository).findById(1);
        verify(chatRepository).findByPatientAndDoctor(testPatient, testDoctor);
        verify(chatMessageRepository).findByChatOrderByCreatedAtAsc(testChat);
        verify(chatMessageRepository).findByChatAndSenderTypeAndReadIsFalse(testChat, MessageSenderType.DOCTOR);
        verify(chatMessageRepository).save(any(ChatMessagesEntity.class));
    }

    @Test
    void C6_getChatMessages_should_throw_exception_when_chat_not_found() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(chatRepository.findByPatientAndDoctor(testPatient, testDoctor)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> chatService.getChatMessages(1, 1, testPatient));

        // Assert
        assertEquals("data_not_found.chat", exception.getCode());

        verify(userRepository).findById(1);
        verify(medicalProfileRepository).findById(1);
        verify(chatRepository).findByPatientAndDoctor(testPatient, testDoctor);
        verify(chatMessageRepository, never()).findByChatOrderByCreatedAtAsc(any());
    }

    @Test
    void C7_sendMessage_should_return_created_message_when_doctor_sends() {
        // Arrange
        ChatMessageRequest request = ChatMessageRequest.builder()
                .message("Hola, ¿cómo te encuentras?")
                .build();

        when(medicalProfileRepository.findById(2)).thenReturn(Optional.of(testDoctor));
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(chatRepository.findByPatientAndDoctor(testPatient, testDoctor)).thenReturn(Optional.of(testChat));
        when(chatMessageRepository.save(any(ChatMessagesEntity.class))).thenReturn(testChatMessage);
        doNothing().when(webSocketService).sendMessageToUser(any(UserEntity.class), any(ChatMessageDTO.class));

        // Act
        ChatMessageDTO result = chatService.sendMessage(2, 1, request, testDoctorUser);

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(testChatMessageDTO.getId(), result.getId());
            assertEquals(testChatMessageDTO.getContent(), result.getContent());
            assertEquals(testChatMessageDTO.getSenderType(), result.getSenderType());
        });

        verify(medicalProfileRepository).findById(2);
        verify(userRepository).findById(1);
        verify(chatRepository).findByPatientAndDoctor(testPatient, testDoctor);
        verify(chatMessageRepository).save(any(ChatMessagesEntity.class));
        verify(webSocketService).sendMessageToUser(eq(testPatient), any(ChatMessageDTO.class));
    }

    @Test
    void C8_sendMessage_should_create_chat_if_not_exists_when_doctor_sends() {
        // Arrange
        ChatMessageRequest request = ChatMessageRequest.builder()
                .message("Hola, ¿cómo te encuentras?")
                .build();

        when(medicalProfileRepository.findById(2)).thenReturn(Optional.of(testDoctor));
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(chatRepository.findByPatientAndDoctor(testPatient, testDoctor)).thenReturn(Optional.empty());
        when(chatRepository.save(any(ChatEntity.class))).thenReturn(testChat);
        when(chatMessageRepository.save(any(ChatMessagesEntity.class))).thenReturn(testChatMessage);
        doNothing().when(webSocketService).sendMessageToUser(any(UserEntity.class), any(ChatMessageDTO.class));

        // Act
        ChatMessageDTO result = chatService.sendMessage(2, 1, request, testDoctorUser);

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(testChatMessageDTO.getId(), result.getId());
            assertEquals(testChatMessageDTO.getContent(), result.getContent());
            assertEquals(testChatMessageDTO.getSenderType(), result.getSenderType());
        });

        verify(medicalProfileRepository).findById(2);
        verify(userRepository).findById(1);
        verify(chatRepository).findByPatientAndDoctor(testPatient, testDoctor);
        verify(chatRepository).save(any(ChatEntity.class));
        verify(chatMessageRepository).save(any(ChatMessagesEntity.class));
        verify(webSocketService).sendMessageToUser(eq(testPatient), any(ChatMessageDTO.class));
    }

    @Test
    void C9_sendMessage_should_return_created_message_when_patient_sends() {
        // Arrange
        ChatMessageRequest request = ChatMessageRequest.builder()
                .message("Hola, ¿cuándo tendré disponible mi informe médico?")
                .build();
        testChatMessage.setSenderType(MessageSenderType.PATIENT);
        testChatMessageDTO = modelMapper.map(testChatMessage, ChatMessageDTO.class);

        when(medicalProfileRepository.findById(2)).thenReturn(Optional.of(testDoctor));
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(chatRepository.findByPatientAndDoctor(testPatient, testDoctor)).thenReturn(Optional.of(testChat));
        when(chatMessageRepository.save(any(ChatMessagesEntity.class))).thenReturn(testChatMessage);
        doNothing().when(webSocketService).sendMessageToUser(any(UserEntity.class), any(ChatMessageDTO.class));

        // Act
        ChatMessageDTO result = chatService.sendMessage(2, 1, request, testPatient);

        // Assert
        assertAll(() -> {
            assertNotNull(result);
            assertEquals(testChatMessageDTO.getId(), result.getId());
            assertEquals(testChatMessageDTO.getContent(), result.getContent());
            assertEquals(MessageSenderType.PATIENT, result.getSenderType());
        });

        verify(medicalProfileRepository).findById(2);
        verify(userRepository).findById(1);
        verify(chatRepository).findByPatientAndDoctor(testPatient, testDoctor);
        verify(chatMessageRepository).save(any(ChatMessagesEntity.class));
        verify(webSocketService).sendMessageToUser(eq(testDoctorUser), any(ChatMessageDTO.class));
    }

    @Test
    void C10_sendMessage_should_throw_exception_when_chat_not_exists_and_patient_sends() {
        // Arrange
        ChatMessageRequest request = ChatMessageRequest.builder()
                .message("Hola doctor, me duele la cabeza")
                .build();

        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(chatRepository.findByPatientAndDoctor(testPatient, testDoctor)).thenReturn(Optional.empty());

        // Act
        DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> chatService.sendMessage(1, 1, request, testPatient));

        // Assert
        assertEquals("data_not_found.chat", exception.getCode());

        verify(medicalProfileRepository).findById(1);
        verify(userRepository).findById(1);
        verify(chatRepository).findByPatientAndDoctor(testPatient, testDoctor);
        verify(chatMessageRepository, never()).save(any(ChatMessagesEntity.class));
        verify(webSocketService, never()).sendMessageToUser(any(), any());
    }

    @Test
    void C11_sendMessage_should_throw_exception_when_sender_is_neither_doctor_nor_patient() {
        // Arrange
        ChatMessageRequest request = ChatMessageRequest.builder()
                .message("Mensaje de test")
                .build();

        UserEntity otherUser = UserEntity.builder()
                .id(3)
                .nombre("Carlos")
                .apellidos("López")
                .build();

        MedicalProfileEntity otherDoctor = MedicalProfileEntity.builder()
                .id(1)
                .user(otherUser)
                .license("COLEG-TEST-3")
                .specialties(new ArrayList<>())
                .build();
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(otherDoctor));
        when(userRepository.findById(3)).thenReturn(Optional.of(otherUser));

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> chatService.sendMessage(1, 3, request, testPatient));

        assertEquals("Sender is neither the doctor nor the patient", exception.getMessage());

        verify(medicalProfileRepository).findById(1);
        verify(userRepository).findById(3);
        verify(chatRepository, never()).findByPatientAndDoctor(testPatient, testDoctor);
        verify(chatRepository, never()).save(any(ChatEntity.class));
        verify(chatMessageRepository, never()).save(any(ChatMessagesEntity.class));
    }

    @Test
    void C12_markMessagesAsRead_should_update_messages() {
        // Arrange
        List<ChatMessagesEntity> unreadMessages = new ArrayList<>(Collections.singletonList(testChatMessage));
        when(chatMessageRepository.findByChatAndSenderTypeAndReadIsFalse(testChat, MessageSenderType.DOCTOR)).thenReturn(unreadMessages);

        // Act
        chatService.markMessagesAsRead(testChat, MessageSenderType.DOCTOR);

        // Assert
        verify(chatMessageRepository).findByChatAndSenderTypeAndReadIsFalse(testChat, MessageSenderType.DOCTOR);
        verify(chatMessageRepository).save(argThat(ChatMessagesEntity::getRead));
    }

    @Test
    void C13_getChatMessages_should_throw_exception_when_request_user_is_neither_doctor_nor_patient() {
        // Arrange
        UserEntity otherUser = UserEntity.builder()
                .id(3)
                .nombre("Carlos")
                .apellidos("López")
                .email("carlosl@salus.com")
                .nif("98765432C")
                .rolesList(new ArrayList<>(List.of(RoleType.USER)))
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(testPatient));
        when(medicalProfileRepository.findById(1)).thenReturn(Optional.of(testDoctor));
        when(chatRepository.findByPatientAndDoctor(testPatient, testDoctor)).thenReturn(Optional.of(testChat));

        // Act
        SecurityException exception = assertThrows(SecurityException.class, () -> chatService.getChatMessages(1, 1, otherUser));

        // Assert
        assertEquals("User does not have access to this chat", exception.getMessage());

        // Verify
        verify(userRepository).findById(1);
        verify(medicalProfileRepository).findById(1);
        verify(chatRepository).findByPatientAndDoctor(testPatient, testDoctor);
        verify(chatMessageRepository, never()).findByChatOrderByCreatedAtAsc(any());
        verify(chatMessageRepository, never()).findByChatAndSenderTypeAndReadIsFalse(any(), any());
    }

    @Test
    void C14_getChatInfo_should_throw_exception_when_request_user_is_neither_doctor_nor_patient() {
        // Arrange
        UserEntity user1 = UserEntity.builder()
                .id(10)
                .nombre("Carlos")
                .apellidos("López")
                .email("carlosl@salus.com")
                .nif("98765432C")
                .rolesList(new ArrayList<>(List.of(RoleType.USER)))
                .build();

        UserEntity user2 = UserEntity.builder()
                .id(11)
                .nombre("María")
                .apellidos("Gómez")
                .email("mariag@salus.com")
                .nif("12345678Z")
                .rolesList(new ArrayList<>(List.of(RoleType.USER, RoleType.PROFESSIONAL)))
                .build();

        MedicalProfileEntity doctor2 = MedicalProfileEntity.builder()
                .id(12)
                .user(user2)
                .license("COLEG-TEST-12")
                .specialties(new ArrayList<>())
                .build();

        when(userRepository.findById(10)).thenReturn(Optional.of(user1));
        when(medicalProfileRepository.findById(12)).thenReturn(Optional.of(doctor2));
        when(chatRepository.findByPatientAndDoctor(user1, doctor2)).thenReturn(Optional.of(testChat));

        // Act
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> chatService.getChatInfo(12, 10, testPatient));

        // Assert
        assertEquals("User is neither the doctor nor the patient of this chat", exception.getMessage());

        verify(userRepository).findById(10);
        verify(medicalProfileRepository).findById(12);
        verify(chatRepository, never()).findByPatientAndDoctor(testPatient, testDoctor);
        verify(chatRepository, never()).save(any(ChatEntity.class));
        verify(chatMessageRepository, never()).findByChatOrderByCreatedAtAsc(any());
        verify(chatMessageRepository, never()).countByChatAndReadFalseAndSenderType(any(), any());
    }
}