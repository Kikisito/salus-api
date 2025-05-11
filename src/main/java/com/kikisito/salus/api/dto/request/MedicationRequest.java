package com.kikisito.salus.api.dto.request;

import com.kikisito.salus.api.constants.ErrorMessages;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MedicationRequest {
    private Integer id;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String name;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String dosage;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    @Digits(integer = 2, fraction = 2, message = ErrorMessages.INVALID_FREQUENCY)
    private BigDecimal frequency;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private LocalDate startDate;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private LocalDate endDate;

    private String instructions;
}
