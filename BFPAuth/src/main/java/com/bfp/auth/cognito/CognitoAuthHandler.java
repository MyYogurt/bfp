package com.bfp.auth.cognito;

import com.bfp.auth.AuthHandler;
import com.bfp.model.AuthenticateRequest;
import com.bfp.model.AuthenticateResponse;
import com.bfp.model.exceptions.UnauthorizedException;
import lombok.NonNull;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;

import java.util.Map;

public class CognitoAuthHandler implements AuthHandler {
    private final CognitoUserPoolClientSecretHashProvider clientSecretHashProvider;

    private final CognitoIdentityProviderClient cognitoIdentityProviderClient;
    private final String userPoolId, userPoolClientId;

    public CognitoAuthHandler(CognitoUserPoolClientSecretHashProvider clientSecretHashProvider,
                              CognitoIdentityProviderClient cognitoIdentityProviderClient, String clientId, String userPoolId) {
        this.clientSecretHashProvider = clientSecretHashProvider;
        this.cognitoIdentityProviderClient = cognitoIdentityProviderClient;
        this.userPoolId = userPoolId;
        this.userPoolClientId = clientId;
    }

    @Override
    public AuthenticateResponse authenticate(@NonNull final AuthenticateRequest authenticateRequest) {
        String secretHash = clientSecretHashProvider.getClientSecretHash(userPoolClientId, authenticateRequest.getUsername());

        Map<String, String> authParameters = Map.of(
                "USERNAME", authenticateRequest.getUsername(),
                "PASSWORD", authenticateRequest.getPassword(),
                "SECRET_HASH", secretHash
        );

        AdminInitiateAuthRequest request = AdminInitiateAuthRequest.builder()
                .userPoolId(userPoolId)
                .clientId(userPoolClientId)
                .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                .authParameters(authParameters)
                .build();

        AdminInitiateAuthResponse cognitoResponse;

        try {
            cognitoResponse = cognitoIdentityProviderClient.adminInitiateAuth(request);
        } catch (NotAuthorizedException notAuthorizedException) {
            throw new UnauthorizedException("Not authorized to perform operation.");
        }

        String accessToken = cognitoResponse.authenticationResult().accessToken();

        if (accessToken == null || accessToken.isBlank()) {
            throw new UnauthorizedException();
        }

        AuthenticateResponse response = AuthenticateResponse.builder()
                .accessToken(accessToken)
                .build();

        return response;
    }
}
