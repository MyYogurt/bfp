package com.bfp.auth.config;

import com.bfp.auth.AuthHandler;
import com.bfp.auth.cognito.CognitoAuthHandler;
import com.bfp.auth.cognito.CognitoUserPoolClientSecretHashProvider;
import com.bfp.auth.cognito.DummyAuthHandler;
import com.bfp.auth.cognito.SecretsManagerHashProvider;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@PropertySource("classpath:server.properties")
@Configuration
public class AuthHandlerConfig {
    @Value("${cognito.userPoolClientId}")
    private String userPoolClientId;

    @Value("${cognito.userPoolId}")
    @NotNull
    private String userPoolId;

//    @Bean
//    @Autowired
//    public AuthHandler getAuthHandler(CognitoIdentityProviderClient cognitoIdentityProviderClient, CognitoUserPoolClientSecretHashProvider cognitoUserPoolClientSecretHashProvider) {
//        return new CognitoAuthHandler(cognitoUserPoolClientSecretHashProvider, cognitoIdentityProviderClient, userPoolClientId, userPoolId);
//    }

    @Bean
    public AuthHandler getAuthHandler() {
        return new DummyAuthHandler();
    }

    @Bean
    public CognitoIdentityProviderClient getCognitoIdentityProviderClient() {
        return CognitoIdentityProviderClient.builder().build();
    }

    @Bean
    public SecretsManagerClient getSecretsManagerClient() {
        return SecretsManagerClient.builder().build();
    }

    @Bean
    @Autowired
    public CognitoUserPoolClientSecretHashProvider getCognitoUserPoolClientSecretHashProvider(SecretsManagerClient secretsManagerClient) {
        return new SecretsManagerHashProvider(secretsManagerClient);
    }
}
