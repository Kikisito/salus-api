package com.kikisito.salus.api.dto.request;

import com.kikisito.salus.api.constants.ErrorMessages;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatMessageRequest {
    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private Integer recipientId;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String message;
}
