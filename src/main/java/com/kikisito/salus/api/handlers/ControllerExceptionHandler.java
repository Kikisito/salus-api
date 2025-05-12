package com.kikisito.salus.api.handlers;

import com.kikisito.salus.api.constants.ErrorMessages;
import com.kikisito.salus.api.dto.ErrorDetails;
import com.kikisito.salus.api.dto.response.ExceptionResponse;
import com.kikisito.salus.api.exception.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ErrorDetails> errorDetails = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> new ErrorDetails("generic.validation_error", this.getMessage(error)))
                .collect(Collectors.toList());

        ExceptionResponse response = new ExceptionResponse(
                "Validation errors occurred",
                errorDetails,
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @Override
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ExceptionResponse response = new ExceptionResponse(
                "Invalid JSON",
                List.of(new ErrorDetails("generic.invalid_json", ErrorMessages.INVALID_JSON)),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @Override
    public ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        logger.error("Payload too large", ex);
        ExceptionResponse response = new ExceptionResponse(
                "Payload too large",
                List.of(new ErrorDetails("generic.payload_too_large", ErrorMessages.PAYLOAD_TOO_LARGE)),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(response);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(DataNotFoundException.class)
    public ExceptionResponse handleDataNotFoundException(final DataNotFoundException ex) {
        logger.error("Data not found", ex);
        return new ExceptionResponse(
                "Data not found",
                List.of(new ErrorDetails(ex.getCode(), ex.getMessage())),
                LocalDateTime.now()
        );
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    public ExceptionResponse handleUnauthorizedException(final UnauthorizedException ex) {
        logger.error("Unauthorized", ex);
        return new ExceptionResponse(
                "You are not authorized to perform this action",
                List.of(new ErrorDetails(ex.getCode(), ex.getMessage())),
                LocalDateTime.now()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidFieldException.class)
    public ExceptionResponse handleInvalidTokenException(final InvalidFieldException ex) {
        logger.error("Invalid token", ex);
        return new ExceptionResponse(
                "Invalid token",
                List.of(new ErrorDetails(ex.getCode(), ex.getMessage())),
                LocalDateTime.now()
        );
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictException.class)
    public ExceptionResponse handleConflictException(final ConflictException ex) {
        logger.error("Conflict occurred", ex);
        return new ExceptionResponse(
                "Conflict occurred",
                List.of(new ErrorDetails(ex.getCode(), ex.getMessage())),
                LocalDateTime.now()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public ExceptionResponse handleBadRequestException(final BadRequestException ex) {
        logger.error("Bad request", ex);
        return new ExceptionResponse(
                "Bad request",
                List.of(new ErrorDetails(ex.getCode(), ex.getMessage())),
                LocalDateTime.now()
        );
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(LockedException.class)
    public ExceptionResponse handleLockedException(final LockedException ex) {
        logger.error("Account locked", ex);
        return new ExceptionResponse(
                "Account locked",
                List.of(new ErrorDetails("generic.account_locked", ErrorMessages.ACCOUNT_LOCKED)),
                LocalDateTime.now()
        );
    }

    private String getMessage(ObjectError error) {
        return String.format(
                error.getDefaultMessage() == null ? ErrorMessages.FIELD_IS_NOT_VALID : error.getDefaultMessage(), ((FieldError) error).getField()
        );
    }
}
