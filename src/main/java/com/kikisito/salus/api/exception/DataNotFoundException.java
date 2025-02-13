package com.kikisito.salus.api.exception;

import com.kikisito.salus.api.constants.ErrorMessages;

public class DataNotFoundException extends ApiRuntimeException {
    public DataNotFoundException(String code, String message) {
        super(code, message);
    }

    public static DataNotFoundException userNotFound() {
        return new DataNotFoundException("data_not_found.user", ErrorMessages.USER_NOT_FOUND);
    }

    public static DataNotFoundException tokenNotFound() {
        return new DataNotFoundException("data_not_found.token", ErrorMessages.TOKEN_NOT_FOUND);
    }
}