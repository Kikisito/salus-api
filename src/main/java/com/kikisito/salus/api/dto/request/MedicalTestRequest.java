package com.kikisito.salus.api.dto.request;

import com.kikisito.salus.api.constants.ErrorMessages;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MedicalTestRequest {
    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private Integer doctor;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private Integer patient;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private Integer specialty;

    private Integer appointment;

    // Se maneja en el controller!
    //private List<MultipartFile> attachments;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String name;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String description;

    private LocalDate requestedAt;

    private LocalDate completedAt;

    private LocalDate scheduledAt;

    private String result;

    private String observations;
}
