package com.kikisito.salus.api.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kikisito.salus.api.dto.ErrorDetails;

import java.time.LocalDateTime;
import java.util.List;

public record ExceptionResponse(
        String message,
        List<ErrorDetails> errors,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp
) {}
