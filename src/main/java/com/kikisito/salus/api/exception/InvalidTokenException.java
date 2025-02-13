package com.kikisito.salus.api.exception;

import com.kikisito.salus.api.constants.ErrorMessages;

public class InvalidTokenException extends ApiRuntimeException {
    public InvalidTokenException(String code, String message) {
        super(code, message);
    }

    public static InvalidTokenException tokenExpired() {
        return new InvalidTokenException("invalid_token.token_expired", ErrorMessages.TOKEN_EXPIRED);
    }

    public static InvalidTokenException tokenAlreadyUsed() {
        return new InvalidTokenException("invalid_token.token_already_used", ErrorMessages.TOKEN_ALREADY_USED);
    }
}