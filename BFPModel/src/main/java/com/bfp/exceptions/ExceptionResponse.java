package com.bfp.exceptions;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
@JsonPropertyOrder({
        ExceptionResponse.JSON_PROPERTY_EXCEPTION_NAME,
        ExceptionResponse.JSON_PROPERTY_MESSAGE
})
public class ExceptionResponse {
    static final String JSON_PROPERTY_EXCEPTION_NAME = "exceptionName";
    static final String JSON_PROPERTY_MESSAGE = "message";

    private String exceptionName;
    private String message;


    ExceptionResponse(Exception exception, String message) {
        this.exceptionName = exception.getClass().getSimpleName();
        this.message = message;
    }

    public static ExceptionResponseBuilder builder() {
        return new ExceptionResponseBuilder();
    }

    ResponseEntity<ExceptionResponse> toResponseEntity(HttpStatus httpStatus) {
        return new ResponseEntity<>(this, httpStatus);
    }

    public static class ExceptionResponseBuilder {
        private Exception exception;
        private String message;

        public ExceptionResponseBuilder exception(Exception exception) {
            this.exception = exception;
            return this;
        }

        public ExceptionResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ExceptionResponse build() {
            return new ExceptionResponse(exception, message);
        }

        public ResponseEntity<ExceptionResponse> toResponseEntity(HttpStatus httpStatus) {
            return new ResponseEntity<>(this.build(), httpStatus);
        }
    }
}