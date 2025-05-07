package com.kikisito.salus.api.exception;

import com.kikisito.salus.api.constants.ErrorMessages;

public class BadRequestException extends ApiRuntimeException {
    public BadRequestException(String code, String message) {
        super(code, message);
    }

    public static BadRequestException attachmentNotFound() {
        return new BadRequestException("data_not_found.attachment", ErrorMessages.ATTACHMENT_NOT_FOUND);
    }

    public static BadRequestException invalidDateOrDateRange() {
        return new BadRequestException("bad_request.invalid_date_or_date_range", ErrorMessages.INVALID_DATE_OR_DATE_RANGE);
    }
}