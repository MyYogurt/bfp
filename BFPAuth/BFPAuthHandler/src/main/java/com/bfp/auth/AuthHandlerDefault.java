package com.bfp.auth;

import com.bfp.auth.model.InitiateAuthRequest;
import com.bfp.auth.model.InitiateAuthResponse;

public class AuthHandlerDefault implements AuthHandler {
    @Override
    public InitiateAuthResponse authenticate(InitiateAuthRequest initiateAuthRequest) {
        InitiateAuthResponse response = new InitiateAuthResponse();
        return response;
    }
}
