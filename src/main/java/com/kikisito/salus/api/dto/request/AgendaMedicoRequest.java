package com.kikisito.salus.api.dto.request;

import com.kikisito.salus.api.constants.ErrorMessages;
import com.kikisito.salus.api.type.DiaSemana;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class AgendaMedicoRequest {
    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private Integer medico;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private DiaSemana diaSemana;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private LocalTime horaInicio;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private LocalTime horaFin;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private Integer duracionCita; // minutos

    @AssertTrue(message = ErrorMessages.END_TIME_MUST_BE_AFTER_START_TIME)
    public boolean isEndTimeAfterStartTime() {
        return horaFin.isAfter(horaInicio);
    }
}
