package com.kikisito.salus.api.exception;

import com.kikisito.salus.api.constants.ErrorMessages;

public class UnauthorizedException extends ApiRuntimeException {
    public static String NOT_AUTHORIZED_KEY = "NOT_AUTHORIZED";

    public UnauthorizedException(String message, String translationKey) {
        super(message, translationKey);
    }

    public static UnauthorizedException notAuthorized() {
        return new UnauthorizedException(ErrorMessages.NOT_AUTHORIZED, NOT_AUTHORIZED_KEY);
    }
}