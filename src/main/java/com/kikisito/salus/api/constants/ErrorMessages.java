package com.kikisito.salus.api.constants;

public final class ErrorMessages {
    public static final String INVALID_JSON = "Invalid JSON. Couldn't parse JSON";
    public static final String FIELD_CANNOT_BE_BLANK = "%s field cannot be empty or null";
    public static final String FIELD_IS_NOT_VALID = "%s field is not valid";
    public static final String INVALID_PASSWORD_FORMAT = "%s field must contain at least one number, one capital letter, one lowercase letter and must be at least 8 characters long";
    public static final String INVALID_EMAIL = "%s must be a correctly formatted email address";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String EMAIL_IS_REGISTERED = "Email is already registered.";
    public static final String NIF_IS_REGISTERED = "User ID is already registered.";
    public static final String EMAIL_IS_VERIFIED = "Email is already verified.";
    public static final String NOT_AUTHORIZED = "Not authorized";
    public static final String PASSWORDS_DONT_MATCH = "Passwords do not match";
    public static final String TOKEN_NOT_FOUND = "Token not found";
    public static final String TOKEN_EXPIRED = "This token has expired before being used";
    public static final String TOKEN_ALREADY_USED = "This token has already been used";
    public static final String INVALID_DATE_PAST = "La fecha de nacimiento debe ser anterior a la fecha actual";
}
