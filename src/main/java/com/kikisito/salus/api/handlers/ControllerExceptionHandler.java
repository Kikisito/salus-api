package com.kikisito.salus.api.handlers;

import com.kikisito.salus.api.constants.ErrorMessages;
import com.kikisito.salus.api.exception.*;
import com.kikisito.salus.api.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@ControllerAdvice
public class ControllerExceptionHandler {
    private final static String METHOD_ARGUMENT_NOT_VALID_KEY = "METHOD_ARGUMENT_NOT_VALID_KEY";
    private final static String INVALID_JSON = "INVALID_JSON";

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public List<ErrorResponse> handleValidationExceptions(final MethodArgumentNotValidException ex) {
        return ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map((error) ->
                        ErrorResponse.of(getMessage(error), METHOD_ARGUMENT_NOT_VALID_KEY)
                )
                .toList();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public List<ErrorResponse> handleHttpMessageNotReadableException(final HttpMessageNotReadableException ex) {
        return List.of(ErrorResponse.of(ErrorMessages.INVALID_JSON, INVALID_JSON));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(DataNotFoundException.class)
    public List<ErrorResponse> handleDataNotFoundException(final DataNotFoundException ex) {
        return ex.toList();
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    public List<ErrorResponse> handleUnauthorizedException(final UnauthorizedException ex) {
        return ex.toList();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidTokenException.class)
    public List<ErrorResponse> handleInvalidTokenException(final InvalidTokenException ex) {
        return ex.toList();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictException.class)
    public List<ErrorResponse> handleConflictException(final ConflictException ex) {
        return ex.toList();
    }

    private static String getMessage(ObjectError error) {
        return String.format(
                error.getDefaultMessage() == null ? ErrorMessages.FIELD_IS_NOT_VALID : error.getDefaultMessage(), ((FieldError) error).getField()
        );
    }
}
