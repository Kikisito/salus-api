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
public class GenerateAppointmentSlotByDateRangeRequest {
    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private LocalDate startDate;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private LocalDate endDate;
}
