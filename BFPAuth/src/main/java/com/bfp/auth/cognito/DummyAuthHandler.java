package com.bfp.auth.cognito;

import com.bfp.auth.AuthHandler;
import com.bfp.model.AuthenticateRequest;
import com.bfp.model.AuthenticateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyAuthHandler implements AuthHandler {
    private final Logger logger = LoggerFactory.getLogger(DummyAuthHandler.class);

    @Override
    public AuthenticateResponse authenticate(AuthenticateRequest AuthenticateRequest) {
        logger.info("Authenticating user: {} with password {}", AuthenticateRequest.getUsername(), AuthenticateRequest.getPassword());

        AuthenticateResponse response = AuthenticateResponse.builder()
                .accessToken("dummy_access_token")
                .build();
        return response;
    }
}
