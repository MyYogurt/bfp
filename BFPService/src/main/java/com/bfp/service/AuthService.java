package com.bfp.service;

import com.bfp.auth.AuthHandler;
import com.bfp.auth.model.InitiateAuthRequest;
import com.bfp.auth.model.InitiateAuthResponse;
import com.bfp.exceptions.InvalidParameterException;
import com.bfp.exceptions.UnauthorizedException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthService {

    private final AuthHandler authHandler;

    @Autowired
    public AuthService(AuthHandler authHandler) {
        this.authHandler = authHandler;
    }

    @GetMapping("/authenticate")
    public InitiateAuthResponse authenticate(@Valid @RequestBody final InitiateAuthRequest authenticateRequest) {
        InitiateAuthResponse response = authHandler.authenticate(authenticateRequest);
        return response;
    }

    @GetMapping("/exception")
    public void exception() {
        throw new InvalidParameterException("This is a test exception");
    }

    @GetMapping("/unauthorized")
    public void unauthorized() {
        throw new UnauthorizedException("Unauthorized!");
    }
}
