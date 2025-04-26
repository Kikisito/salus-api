package com.kikisito.salus.api.exception;

import com.kikisito.salus.api.constants.ErrorMessages;

public class BadRequestException extends ApiRuntimeException {
    public BadRequestException(String code, String message) {
        super(code, message);
    }

    public static BadRequestException attachmentNotFound() {
        return new BadRequestException("data_not_found.attachment", ErrorMessages.ATTACHMENT_NOT_FOUND);
    }
}