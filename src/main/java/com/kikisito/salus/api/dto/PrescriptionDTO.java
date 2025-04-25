package com.kikisito.salus.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PrescriptionDTO {
    private Integer id;
    private MedicalProfileDTO doctor;
    private UserDTO patient;
    private SpecialtyDTO specialty;
    private Integer appointmentId;
    private List<MedicationDTO> medications;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
