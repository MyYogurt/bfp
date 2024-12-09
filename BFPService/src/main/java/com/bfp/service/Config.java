package com.bfp.service;

import com.bfp.auth.AuthHandler;
import com.bfp.auth.cognito.CognitoAuthHandler;
import com.bfp.auth.cognito.CognitoUserPoolClientSecretHashProvider;
import com.bfp.auth.cognito.SecretsManagerHashProvider;
import com.bfp.filemanagement.FileHandler;
import com.bfp.filemanagement.dao.FileDAO;
import com.bfp.filemanagement.dao.PostgresFileDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

//@PropertySource("classpath:application.yaml")
@Configuration
public class Config {
    @Value("${cognito.userPoolClientId}")
    private String userPoolClientId;

    @Value("${cognito.userPoolId}")
    private String userPoolId;

    @Value("${fileBucketName}")
    private String fileBucketName;

    @Bean
    @Autowired
    public AuthHandler getAuthHandler(CognitoIdentityProviderClient cognitoIdentityProviderClient,
                                      CognitoUserPoolClientSecretHashProvider cognitoUserPoolClientSecretHashProvider) {
        return new CognitoAuthHandler(cognitoUserPoolClientSecretHashProvider, cognitoIdentityProviderClient,
                userPoolClientId, userPoolId);
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

    @Bean
    public S3Client getS3Client() {
        return S3Client.builder().build();
    }

    @Bean
    public FileDAO getFileDAO() {
        return new PostgresFileDAO();
    }

    @Bean
    @Autowired
    public FileHandler getFileHandler(FileDAO fileDAO, S3Client s3Client) {
        return new FileHandler(fileDAO, s3Client, fileBucketName);
    }
}