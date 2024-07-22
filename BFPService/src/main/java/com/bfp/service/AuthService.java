package com.bfp.service;

import com.bfp.auth.AuthHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthService {

    private final AuthHandler authHandler;

    @Autowired
    public AuthService(AuthHandler authHandler) {
        this.authHandler = authHandler;
    }


}
