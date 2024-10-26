package com.bfp.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseWebException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.UNAUTHORIZED;

    public UnauthorizedException(String message) {
        super(message, HTTP_STATUS);
    }
}
