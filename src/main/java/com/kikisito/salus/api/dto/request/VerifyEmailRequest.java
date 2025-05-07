package com.kikisito.salus.api.dto.request;

import com.kikisito.salus.api.constants.ErrorMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class VerifyEmailRequest {
    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String token;
}
