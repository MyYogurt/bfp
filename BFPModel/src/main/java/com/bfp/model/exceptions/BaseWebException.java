package com.bfp.model.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseWebException extends RuntimeException {
    protected final HttpStatus httpStatus;

    public BaseWebException(String message, HttpStatus  httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public ResponseEntity<ExceptionResponse> toResponseEntity() {
        return ExceptionResponse.builder()
                .exception(this)
                .message(getMessage())
                .toResponseEntity(httpStatus);
    }
}
