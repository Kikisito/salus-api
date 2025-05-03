package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.ChatEntity;
import com.kikisito.salus.api.entity.ChatMessagesEntity;
import com.kikisito.salus.api.type.MessageSenderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessagesEntity, Integer> {
    List<ChatMessagesEntity> findByChatOrderByCreatedAtAsc(ChatEntity chat);

    int countByChatAndReadFalseAndSenderType(ChatEntity chat, MessageSenderType senderType);
}
