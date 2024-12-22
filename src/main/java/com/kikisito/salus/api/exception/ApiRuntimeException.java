package com.kikisito.salus.api.exception;

import com.kikisito.salus.api.response.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@Getter
public class ApiRuntimeException extends RuntimeException {
    private String translationKey;
    private String message;

    public List<ErrorResponse> toList() {
        return Collections.singletonList(ErrorResponse.of(message, translationKey));
    }
}
