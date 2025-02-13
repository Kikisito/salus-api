package com.kikisito.salus.api.exception;

import com.kikisito.salus.api.dto.ErrorDetails;
import com.kikisito.salus.api.dto.response.ExceptionResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@Getter
public class ApiRuntimeException extends RuntimeException {
    private String code;
    private String message;

    public List<ExceptionResponse> toList() {
        ErrorDetails errorDetails = new ErrorDetails(code, message);
        ExceptionResponse exceptionResponse = new ExceptionResponse(message, Collections.singletonList(errorDetails), LocalDateTime.now());
        return Collections.singletonList(exceptionResponse);
    }
}
