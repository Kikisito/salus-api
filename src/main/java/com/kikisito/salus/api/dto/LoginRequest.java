package com.kikisito.salus.api.dto;

import com.kikisito.salus.api.constants.ErrorMessages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequest {
    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    @Email(message = ErrorMessages.INVALID_EMAIL)
    private String email;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String password;
}
