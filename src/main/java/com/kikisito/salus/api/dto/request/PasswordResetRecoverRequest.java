package com.kikisito.salus.api.dto.request;

import com.kikisito.salus.api.constants.ErrorMessages;
import com.kikisito.salus.api.constants.PasswordConstraint;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PasswordResetRecoverRequest {
    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String token;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    @PasswordConstraint(message = ErrorMessages.INVALID_PASSWORD_FORMAT)
    private String password;
}
