package com.kikisito.salus.api.exception;

import com.kikisito.salus.api.constants.ErrorMessages;

public class ConflictException extends ApiRuntimeException {
    public ConflictException(String code, String message) {
        super(code, message);
    }

    public static ConflictException emailIsRegistered() {
        return new ConflictException("conflict.email_is_registered", ErrorMessages.EMAIL_IS_REGISTERED);
    }

    public static ConflictException nifIsRegistered() {
        return new ConflictException("conflict.nif_is_registered", ErrorMessages.NIF_IS_REGISTERED);
    }

    public static ConflictException emailIsVerified() {
        return new ConflictException("conflict.email_is_verified", ErrorMessages.EMAIL_IS_VERIFIED);
    }

    public static ConflictException userAlreadyMedico() {
        return new ConflictException("conflict.user_already_medico", ErrorMessages.USER_ALREADY_MEDICO);
    }

    public static ConflictException numeroColegiadoAlreadyExists() {
        return new ConflictException("conflict.numero_colegiado_already_exists", ErrorMessages.NUMERO_COLEGIADO_ALREADY_EXISTS);
    }

    public static ConflictException medicoAlreadyHasEspecialidad() {
        return new ConflictException("conflict.medico_already_has_especialidad", ErrorMessages.MEDICO_ALREADY_HAS_ESPECIALIDAD);
    }

    public static ConflictException doctorDoesNotHaveSpecialty() {
        return new ConflictException("conflict.doctor_does_not_have_specialty", ErrorMessages.DOCTOR_DOES_NOT_HAVE_SPECIALTY);
    }

    public static ConflictException horarioColapsa() {
        return new ConflictException("conflict.horario_colapsa", ErrorMessages.HORARIO_COLAPSA);
    }

    public static ConflictException appointmentSlotIsAlreadyTaken() {
        return new ConflictException("conflict.appointment_slot_is_already_taken", ErrorMessages.APPOINTMENT_SLOT_IS_ALREADY_TAKEN);
    }
}
