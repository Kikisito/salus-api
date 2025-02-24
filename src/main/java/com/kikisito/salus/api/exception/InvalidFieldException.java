package com.kikisito.salus.api.exception;

import com.kikisito.salus.api.constants.ErrorMessages;

public class InvalidFieldException extends ApiRuntimeException {
    public InvalidFieldException(String code, String message) {
        super(code, message);
    }

    public static InvalidFieldException tokenExpired() {
        return new InvalidFieldException("invalid_field.token_expired", ErrorMessages.TOKEN_EXPIRED);
    }

    public static InvalidFieldException tokenAlreadyUsed() {
        return new InvalidFieldException("invalid_field.token_already_used", ErrorMessages.TOKEN_ALREADY_USED);
    }

    public static InvalidFieldException invalidPassword() {
        return new InvalidFieldException("invalid_field.incorrect_password", ErrorMessages.INVALID_PASSWORD);
    }
}