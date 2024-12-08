package com.bfp.service;

import com.bfp.auth.AuthHandler;
import com.bfp.common.CommonRequestHelper;
import com.bfp.model.AuthenticateRequest;
import com.bfp.model.AuthenticateResponse;
import com.bfp.model.exceptions.InvalidParameterException;
import com.bfp.model.exceptions.UnauthorizedException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthService {

    private final AuthHandler authHandler;

    @Autowired
    public AuthService(AuthHandler authHandler) {
        this.authHandler = authHandler;
    }

    @GetMapping("/authenticate")
    public AuthenticateResponse authenticate(@Valid @RequestBody final AuthenticateRequest authenticateRequest) {
        AuthenticateResponse response = authHandler.authenticate(authenticateRequest);
        return response;
    }

    @GetMapping("/hello")
    public String hello() {
        return CommonRequestHelper.getUserId();
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
