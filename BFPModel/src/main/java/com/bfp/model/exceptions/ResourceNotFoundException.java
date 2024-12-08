package com.bfp.model.exceptions;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseWebException {
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST;
    private static final String DEFAULT_MESSAGE = "Resource not found.";

    public ResourceNotFoundException(String message) {
        super(message, HTTP_STATUS);
    }

    public ResourceNotFoundException() {
        this(DEFAULT_MESSAGE);
    }
}
