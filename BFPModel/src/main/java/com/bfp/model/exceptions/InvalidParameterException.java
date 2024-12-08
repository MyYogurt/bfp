package com.bfp.model.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidParameterException extends BaseWebException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST;

    public InvalidParameterException(String message) {
        super(message, HTTP_STATUS);
    }
}
