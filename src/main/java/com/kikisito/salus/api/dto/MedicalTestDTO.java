package com.kikisito.salus.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MedicalTestDTO {
    private Integer id;
    private MedicalProfileDTO doctor;
    private UserDTO patient;
    private SpecialtyDTO specialty;
    private Integer appointmentId;
    private List<AttachmentDTO> attachments;
    private String name;
    private String description;
    private LocalDate requestedAt;
    private LocalDate completedAt;
    private LocalDate scheduledAt;
    private String result;
    private String observations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
