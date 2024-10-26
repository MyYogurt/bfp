package com.bfp.auth.cognito;

import com.bfp.auth.AuthHandler;
import com.bfp.auth.model.InitiateAuthRequest;
import com.bfp.auth.model.InitiateAuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyAuthHandler implements AuthHandler {
    private final Logger logger = LoggerFactory.getLogger(DummyAuthHandler.class);

    @Override
    public InitiateAuthResponse authenticate(InitiateAuthRequest initiateAuthRequest) {
        logger.info("Authenticating user: {} with password {}", initiateAuthRequest.getUsername(), initiateAuthRequest.getPassword());

        InitiateAuthResponse response = InitiateAuthResponse.builder()
                .accessToken("dummy_access_token")
                .build();
        return response;
    }
}
