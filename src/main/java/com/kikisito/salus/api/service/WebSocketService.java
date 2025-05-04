package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.ChatMessageDTO;
import com.kikisito.salus.api.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketService {
    @Autowired
    private final SimpMessagingTemplate messagingTemplate;

    public void sendMessageToUser(UserEntity user, ChatMessageDTO message) {
        messagingTemplate.convertAndSendToUser(user.getUsername(), "/queue/messages", message);
    }
}