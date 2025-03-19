package com.kikisito.salus.api.dto.request;

import com.kikisito.salus.api.constants.ErrorMessages;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddEspecialidadMedicoRequest {
    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private Integer especialidadId;
}
