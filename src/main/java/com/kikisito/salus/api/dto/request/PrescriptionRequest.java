package com.kikisito.salus.api.dto.request;

import com.kikisito.salus.api.constants.ErrorMessages;
import com.kikisito.salus.api.dto.*;
import com.kikisito.salus.api.type.ReportType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PrescriptionRequest {
    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private Integer doctor;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private Integer patient;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private Integer specialty;

    private Integer appointment;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    @NotEmpty(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    @Valid
    private List<MedicationRequest> medications;
}
