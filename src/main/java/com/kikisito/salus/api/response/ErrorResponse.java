package com.kikisito.salus.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class ErrorResponse {
    private String translationKey;
    private String message;

    public static ErrorResponse of(String message, String translationKey) {
        return new ErrorResponse(message, translationKey);
    }
}
