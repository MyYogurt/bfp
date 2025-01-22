package com.bfp.service;

import com.bfp.auth.BFPUser;
import com.bfp.model.exceptions.UnauthorizedException;
import com.bfp.service.security.CustomJwtAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;

import java.util.Map;

public class CommonRequestHelper {

    public String getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if (principal instanceof Jwt jwt) {
            return jwt.getClaim("sub");
        }

        throw new RuntimeException("Could not get user id.");
    }

    public String getAccessToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if (principal instanceof Jwt jwt) {
            return jwt.getTokenValue();
        }

        throw new RuntimeException("Could not get access key.");
    }

    public BFPUser getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getDetails() instanceof BFPUser ? (BFPUser) auth.getDetails() : null;
    }
}
