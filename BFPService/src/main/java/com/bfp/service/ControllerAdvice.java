package com.bfp.service;

import com.bfp.model.exceptions.BaseWebException;
import com.bfp.model.exceptions.ExceptionResponse;
import com.bfp.model.exceptions.InvalidParameterException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler
    ResponseEntity<ExceptionResponse> handleExceptions(
            Exception ex) {
        return switch (ex) {
            case MethodArgumentNotValidException e -> handleMethodArgumentNotValidException(e);
            case NoResourceFoundException e -> handleNoResourceFoundException(e);
            case BaseWebException e -> e.toResponseEntity();
            default -> throw new IllegalStateException("Unexpected value: " + ex);
        };
    }

    private ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException methodArgumentNotValidException) {
        Map<String, String> errors = new HashMap<>();
        methodArgumentNotValidException.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ExceptionResponse
                .builder()
                .exception(methodArgumentNotValidException)
                .message(errors.toString())
                .toResponseEntity(HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ExceptionResponse> handleNoResourceFoundException(NoResourceFoundException noResourceFoundException) {
        return ExceptionResponse
                .builder()
                .exception(noResourceFoundException)
                .message(String.format(
                        "No resource for path: %s %s",
                        noResourceFoundException.getHttpMethod(),
                        noResourceFoundException.getResourcePath()
                ))
                .toResponseEntity(HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<ExceptionResponse> handleInvalidParameterException(InvalidParameterException invalidParameterException) {
        return ExceptionResponse
                .builder()
                .exception(invalidParameterException)
                .message(invalidParameterException.getMessage())
                .toResponseEntity(HttpStatus.BAD_REQUEST);
    }
}
