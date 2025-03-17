package com.kikisito.salus.api.constants;

public final class ErrorMessages {
    public static final String INVALID_JSON = "Invalid JSON. Couldn't parse JSON";
    public static final String FIELD_CANNOT_BE_BLANK = "%s field cannot be empty or null";
    public static final String FIELD_IS_NOT_VALID = "%s field is not valid";
    public static final String FIELD_LENGTH_INVALID = "%s field must be between {min} and {max} characters long";
    public static final String INVALID_PASSWORD_FORMAT = "%s field must contain at least one number, one capital letter, one lowercase letter and must be at least 8 characters long";
    public static final String INVALID_PASSWORD = "Invalid password";
    public static final String INVALID_EMAIL = "%s must be a correctly formatted email address";
    public static final String INVALID_NIF = "%s must be a valid NIF/NIE";
    public static final String USER_NOT_FOUND = "User could not be found";
    public static final String EMAIL_IS_REGISTERED = "Email is already registered.";
    public static final String NIF_IS_REGISTERED = "User ID is already registered.";
    public static final String EMAIL_IS_VERIFIED = "Email is already verified.";
    public static final String NOT_AUTHORIZED = "Not authorized";
    public static final String PASSWORDS_DONT_MATCH = "Passwords do not match";
    public static final String TOKEN_NOT_FOUND = "Token not found";
    public static final String TOKEN_EXPIRED = "This token has expired before being used";
    public static final String TOKEN_ALREADY_USED = "This token has already been used";
    public static final String INVALID_DATE_PAST = "La fecha de nacimiento debe ser anterior a la fecha actual";
    public static final String INVALID_DATE_18_YEARS_OLD = "El usuario debe tener al menos 18 años";
    public static final String CENTRO_MEDICO_NOT_FOUND = "Centro médico no encontrado";
    public static final String CONSULTA_NOT_FOUND = "Consulta no encontrada";
    public static final String USER_ALREADY_MEDICO = "User is already registered as a doctor";
    public static final String NUMERO_COLEGIADO_ALREADY_EXISTS = "This medical registration number is already registered";
    public static final String MEDICO_NOT_FOUND = "Médico no encontrado";
    public static final String ESPECIALIDAD_NOT_FOUND = "Especialidad no encontrada";
    public static final String MEDICO_ALREADY_HAS_ESPECIALIDAD = "El médico ya tiene asignada la especialidad";
    public static final String END_TIME_MUST_BE_AFTER_START_TIME = "End time must be after start time";
    public static final String HORARIO_COLAPSA = "El horario colapsa con otro horario existente";
    public static final String AGENDA_NOT_FOUND = "Agenda no encontrada";
    public static final String CITA_SLOT_NOT_FOUND = "Cita slot no encontrada";
    public static final String CITA_NOT_FOUND = "Cita no encontrada";
    public static final String APPOINTMENT_SLOT_IS_ALREADY_TAKEN = "Appointment slot is already taken";
}
