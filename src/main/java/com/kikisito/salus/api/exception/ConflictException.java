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

    public static ConflictException userAlreadyDoctor() {
        return new ConflictException("conflict.user_already_medico", ErrorMessages.USER_ALREADY_DOCTOR);
    }

    public static ConflictException licenseAlreadyExists() {
        return new ConflictException("conflict.numero_colegiado_already_exists", ErrorMessages.LICENSE_ALREADY_EXISTS);
    }

    public static ConflictException doctorHasAlreadySpecialty() {
        return new ConflictException("conflict.medico_already_has_especialidad", ErrorMessages.DOCTOR_ALREADY_HAS_SPECIALTY);
    }

    public static ConflictException doctorDoesNotHaveSpecialty() {
        return new ConflictException("conflict.doctor_does_not_have_specialty", ErrorMessages.DOCTOR_DOES_NOT_HAVE_SPECIALTY);
    }

    public static ConflictException scheduleConflict() {
        return new ConflictException("conflict.schedule_conflict", ErrorMessages.SCHEDULE_CONFLICT);
    }

    public static ConflictException appointmentSlotIsAlreadyTaken() {
        return new ConflictException("conflict.appointment_slot_is_already_taken", ErrorMessages.APPOINTMENT_SLOT_IS_ALREADY_TAKEN);
    }

    public static ConflictException doctorHasMedicalDataLinked() {
        return new ConflictException("conflict.doctor_has_appointments", ErrorMessages.DOCTOR_HAS_MEDICAL_DATA_LINKED);
    }

    public static ConflictException appointmentCannotBeDeleted() {
        return new ConflictException("conflict.appointment_cannot_be_deleted", ErrorMessages.APPOINTMENT_CANNOT_BE_DELETED);
    }

    public static ConflictException dateInPast() {
        return new ConflictException("conflict.date_is_in_the_past", ErrorMessages.DATE_IN_PAST);
    }

    public static ConflictException cannotCreateChatWithSameSenderAndReceiver() {
        return new ConflictException("conflict.cannot_create_chat_with_same_sender_and_receiver", ErrorMessages.CANNOT_CREATE_CHAT_WITH_SAME_SENDER_AND_RECEIVER);
    }

    public static ConflictException dayMismatch() {
        return new ConflictException("conflict.day_mismatch", ErrorMessages.DAY_MISMATCH);
    }

    public static ConflictException appointmentSlotCannotBeBookedByDoctor() {
        return new ConflictException("conflict.appointment_slot_cannot_be_booked_by_doctor", ErrorMessages.APPOINTMENT_SLOT_CANNOT_BE_BOOKED_BY_DOCTOR);
    }
}
