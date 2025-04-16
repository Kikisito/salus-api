package com.kikisito.salus.api.dto.request;

import com.kikisito.salus.api.constants.ErrorMessages;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@Builder
public class DoctorScheduleRequest {
    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private Integer doctor;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private Integer specialty;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private Integer room;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private DayOfWeek dayOfWeek;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private LocalTime startTime;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private LocalTime endTime;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private Integer duration; // minutes

    @AssertTrue(message = ErrorMessages.END_TIME_MUST_BE_AFTER_START_TIME)
    public boolean isEndTimeAfterStartTime() {
        return endTime.isAfter(startTime);
    }
}
