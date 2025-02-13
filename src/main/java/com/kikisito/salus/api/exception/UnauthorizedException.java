package com.kikisito.salus.api.exception;

import com.kikisito.salus.api.constants.ErrorMessages;

public class UnauthorizedException extends ApiRuntimeException {
    public UnauthorizedException(String code, String message) {
        super(code, message);
    }

    public static UnauthorizedException notAuthorized() {
        return new UnauthorizedException("not_authorized.not_authorized", ErrorMessages.NOT_AUTHORIZED);
    }
}