package com.kikisito.salus.api.dto.request;

import com.kikisito.salus.api.constants.ErrorMessages;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AppointmentSlotRequest {
    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private Integer doctor;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private Integer specialty;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private Integer room;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private LocalDate date;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private LocalTime startTime;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private LocalTime endTime;
}
