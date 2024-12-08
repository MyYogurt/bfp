package com.bfp.auth;

import com.bfp.model.AuthenticateRequest;
import com.bfp.model.AuthenticateResponse;

public interface AuthHandler {
    AuthenticateResponse authenticate(AuthenticateRequest AuthenticateRequest);
}
