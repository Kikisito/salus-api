package com.kikisito.salus.api.dto;

import com.kikisito.salus.api.type.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReportDTO {
    private Integer id;
    private ReportType type;
    private SpecialtyDTO specialty;
    private Integer appointmentId;
    private MedicalProfileDTO doctor;
    private UserDTO patient;
    private String description;
    private String diagnosis;
    private String treatment;
    private String observations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
