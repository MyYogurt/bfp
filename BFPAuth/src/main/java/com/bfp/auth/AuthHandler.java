package com.bfp.auth;

import com.bfp.auth.model.InitiateAuthRequest;
import com.bfp.auth.model.InitiateAuthResponse;

public interface AuthHandler {
    InitiateAuthResponse authenticate(InitiateAuthRequest initiateAuthRequest);
}
