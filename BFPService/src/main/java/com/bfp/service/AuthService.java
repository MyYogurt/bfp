package com.bfp.service;

import com.bfp.auth.AuthHandler;
import com.bfp.auth.model.InitiateAuthRequest;
import com.bfp.auth.model.InitiateAuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
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
    public InitiateAuthResponse authenticate(@NonNull @RequestBody final InitiateAuthRequest initiateAuthRequest) {
        InitiateAuthResponse response = authHandler.authenticate(initiateAuthRequest);
        return response;
    }
}
