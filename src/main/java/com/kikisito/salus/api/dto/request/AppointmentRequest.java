package com.kikisito.salus.api.dto.request;

import com.kikisito.salus.api.constants.ErrorMessages;
import com.kikisito.salus.api.type.CitaType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AppointmentRequest {
    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private Integer appointmentSlot;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private Integer patient;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private CitaType type; // Presencial, telefónica...

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String reason;
}
