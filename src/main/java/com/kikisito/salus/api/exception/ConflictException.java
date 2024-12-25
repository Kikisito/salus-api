package com.kikisito.salus.api.exception;

import com.kikisito.salus.api.constants.ErrorMessages;

public class ConflictException extends ApiRuntimeException {
    public static String EMAIL_IS_REGISTERED = "CONFLICT_EXCEPTION.EMAIL_IS_REGISTERED";
    public static String NIF_IS_REGISTERED = "CONFLICT_EXCEPTION.NIF_IS_REGISTERED";
    public static String EMAIL_IS_VERIFIED = "CONFLICT_EXCEPTION.EMAIL_IS_VERIFIED";

    public ConflictException(String message, String translationKey) {
        super(message, translationKey);
    }

    public static ConflictException emailIsRegistered() {
        return new ConflictException(ErrorMessages.EMAIL_IS_REGISTERED, EMAIL_IS_REGISTERED);
    }

    public static ConflictException nifIsRegistered() {
        return new ConflictException(ErrorMessages.NIF_IS_REGISTERED, NIF_IS_REGISTERED);
    }

    public static ConflictException emailIsVerified() {
        return new ConflictException(ErrorMessages.EMAIL_IS_VERIFIED, EMAIL_IS_VERIFIED);
    }
}
