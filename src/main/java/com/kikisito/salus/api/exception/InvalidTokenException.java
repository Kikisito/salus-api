package com.kikisito.salus.api.exception;

import com.kikisito.salus.api.constants.ErrorMessages;

public class InvalidTokenException extends ApiRuntimeException {

    public static String TOKEN_EXPIRED = "INVALID_TOKEN_EXCEPTION.TOKEN_EXPIRED";
    public static String TOKEN_ALREADY_USED = "INVALID_TOKEN_EXCEPTION.TOKEN_USED";

    public InvalidTokenException(String message, String translationKey) {
        super(message, translationKey);
    }

    public static InvalidTokenException tokenExpired() {
        return new InvalidTokenException(ErrorMessages.TOKEN_EXPIRED, TOKEN_EXPIRED);
    }

    public static InvalidTokenException tokenAlreadyUsed() {
        return new InvalidTokenException(ErrorMessages.TOKEN_ALREADY_USED, TOKEN_ALREADY_USED);
    }
}