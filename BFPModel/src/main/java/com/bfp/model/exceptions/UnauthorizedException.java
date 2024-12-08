package com.bfp.model.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseWebException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.UNAUTHORIZED;
    private static final String DEFAULT_MESSAGE = "Not authorized to perform operation.";

    public UnauthorizedException(String message) {
        super(message, HTTP_STATUS);
    }

    public UnauthorizedException() {
        this(DEFAULT_MESSAGE);
    }
}
