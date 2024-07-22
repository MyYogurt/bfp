package com.bfp.auth;

import com.bfp.auth.model.InitiateAuthRequest;
import com.bfp.auth.model.InitiateAuthResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

public class AuthHandlerDefault implements AuthHandler {
    @Override
    public InitiateAuthResponse authenticate(@NonNull final InitiateAuthRequest initiateAuthRequest) {
        InitiateAuthResponse response = InitiateAuthResponse.builder()
            .accessToken("dummy-access-token")
            .build();
        return response;
    }
}
