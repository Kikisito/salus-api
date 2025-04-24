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
        return new DataNotFoundException("data_not_found.centro_medico", ErrorMessages.MEDICAL_CENTER_NOT_FOUND);
    }

    public static DataNotFoundException roomNotFound() {
        return new DataNotFoundException("data_not_found.consulta", ErrorMessages.ROOM_NOT_FOUND);
    }

    public static DataNotFoundException doctorNotFound() {
        return new DataNotFoundException("data_not_found.medico", ErrorMessages.DOCTOR_NOT_FOUND);
    }

    public static DataNotFoundException specialtyNotFound() {
        return new DataNotFoundException("data_not_found.especialidad", ErrorMessages.SPECIALTY_NOT_FOUND);
    }

    public static DataNotFoundException scheduleNotFound() {
        return new DataNotFoundException("data_not_found.agenda", ErrorMessages.SCHEDULE_NOT_FOUND);
    }

    public static DataNotFoundException appointmentSlotNotFound() {
        return new DataNotFoundException("data_not_found.cita_slot", ErrorMessages.APPOINTMENT_SLOT_NOT_FOUND);
    }

    public static DataNotFoundException appointmentNotFound() {
        return new DataNotFoundException("data_not_found.cita", ErrorMessages.APPOINTMENT_NOT_FOUND);
    }

    public static DataNotFoundException reportNotFound() {
        return new DataNotFoundException("data_not_found.informe", ErrorMessages.REPORT_NOT_FOUND);
    }
}