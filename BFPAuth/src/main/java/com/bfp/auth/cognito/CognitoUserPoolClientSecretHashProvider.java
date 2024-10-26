package com.bfp.auth.cognito;

public interface CognitoUserPoolClientSecretHashProvider {
    String getClientSecretHash(String clientId, String userName);
}
