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

    public static DataNotFoundException medicalCenterNotFound() {
        return new DataNotFoundException("data_not_found.medical_center", ErrorMessages.MEDICAL_CENTER_NOT_FOUND);
    }

    public static DataNotFoundException roomNotFound() {
        return new DataNotFoundException("data_not_found.room", ErrorMessages.ROOM_NOT_FOUND);
    }

    public static DataNotFoundException doctorNotFound() {
        return new DataNotFoundException("data_not_found.doctor", ErrorMessages.DOCTOR_NOT_FOUND);
    }

    public static DataNotFoundException specialtyNotFound() {
        return new DataNotFoundException("data_not_found.specialty", ErrorMessages.SPECIALTY_NOT_FOUND);
    }

    public static DataNotFoundException scheduleNotFound() {
        return new DataNotFoundException("data_not_found.schedule", ErrorMessages.SCHEDULE_NOT_FOUND);
    }

    public static DataNotFoundException appointmentSlotNotFound() {
        return new DataNotFoundException("data_not_found.appointment_slot", ErrorMessages.APPOINTMENT_SLOT_NOT_FOUND);
    }

    public static DataNotFoundException appointmentNotFound() {
        return new DataNotFoundException("data_not_found.appointment", ErrorMessages.APPOINTMENT_NOT_FOUND);
    }

    public static DataNotFoundException reportNotFound() {
        return new DataNotFoundException("data_not_found.report", ErrorMessages.REPORT_NOT_FOUND);
    }

    public static DataNotFoundException prescriptionNotFound() {
        return new DataNotFoundException("data_not_found.prescription", ErrorMessages.PRESCRIPTION_NOT_FOUND);
    }

    public static DataNotFoundException medicalTestNotFound() {
        return new DataNotFoundException("data_not_found.medical_test", ErrorMessages.MEDICAL_TEST_NOT_FOUND);
    }

    public static DataNotFoundException attachmentNotFound() {
        return new DataNotFoundException("data_not_found.attachment", ErrorMessages.ATTACHMENT_NOT_FOUND);
    }

    public static DataNotFoundException medicationNotFound() {
        return new DataNotFoundException("data_not_found.medication", ErrorMessages.MEDICATION_NOT_FOUND);
    }
}