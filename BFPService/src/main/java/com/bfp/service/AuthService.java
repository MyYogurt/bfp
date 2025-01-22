package com.bfp.service;

import com.bfp.auth.AuthHandler;
import com.bfp.model.AuthenticateRequest;
import com.bfp.model.AuthenticateResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthService {
    @Autowired
    private CommonRequestHelper commonRequestHelper;

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

    @GetMapping("/logout")
    public void logout() {
        authHandler.signOut(commonRequestHelper.getAccessToken());
    }

    @GetMapping("/hello")
    public String hello() {
        return commonRequestHelper.getUserId();
    }
}
