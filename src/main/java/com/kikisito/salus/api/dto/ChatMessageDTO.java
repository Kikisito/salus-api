package com.kikisito.salus.api.dto;

import com.kikisito.salus.api.type.MessageSenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatMessageDTO {
    private Integer id;
    private Integer chatId;
    private MessageSenderType senderType;
    private String content;
    private String createdAt; // sentAt
    private Boolean read;
}
