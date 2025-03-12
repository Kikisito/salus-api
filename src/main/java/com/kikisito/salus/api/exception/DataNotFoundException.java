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

    public static DataNotFoundException centroMedicoNotFound() {
        return new DataNotFoundException("data_not_found.centro_medico", ErrorMessages.CENTRO_MEDICO_NOT_FOUND);
    }

    public static DataNotFoundException consultaNotFound() {
        return new DataNotFoundException("data_not_found.consulta", ErrorMessages.CONSULTA_NOT_FOUND);
    }
}