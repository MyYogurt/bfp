package com.bfp.auth;

import com.bfp.auth.model.InitiateAuthRequest;

public interface AuthHandler {
    void authenticate(InitiateAuthRequest initiateAuthRequest);
}
