package com.kikisito.salus.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatDTO {
    private Integer id;
    private ReducedUserDTO patient;
    private MedicalProfileDTO doctor;
    private String lastMessage;
    private Integer unreadMessages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
