package com.bfp.cdk;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apprunner.alpha.AutoScalingConfiguration;
import software.amazon.awscdk.services.apprunner.alpha.Cpu;
import software.amazon.awscdk.services.apprunner.alpha.EcrProps;
import software.amazon.awscdk.services.apprunner.alpha.HealthCheck;
import software.amazon.awscdk.services.apprunner.alpha.HttpHealthCheckOptions;
import software.amazon.awscdk.services.apprunner.alpha.ImageConfiguration;
import software.amazon.awscdk.services.apprunner.alpha.Memory;
import software.amazon.awscdk.services.apprunner.alpha.Service;
import software.amazon.awscdk.services.apprunner.alpha.Source;
import software.amazon.awscdk.services.cognito.AccountRecovery;
import software.amazon.awscdk.services.cognito.AuthFlow;
import software.amazon.awscdk.services.cognito.Mfa;
import software.amazon.awscdk.services.cognito.PasswordPolicy;
import software.amazon.awscdk.services.cognito.SignInAliases;
import software.amazon.awscdk.services.cognito.StandardAttribute;
import software.amazon.awscdk.services.cognito.StandardAttributes;
import software.amazon.awscdk.services.cognito.UserPool;
import software.amazon.awscdk.services.cognito.UserPoolClient;
import software.amazon.awscdk.services.cognito.UserPoolEmail;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyDocument;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.rds.DatabaseCluster;
import software.amazon.awscdk.services.rds.DatabaseClusterEngine;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.secretsmanager.Secret;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class BFPServiceStack extends StagedStack {
    public BFPServiceStack(final Construct parent, final String id, String stage) {
        this(parent, id, null, stage);
    }

    public BFPServiceStack(final Construct parent, final String id, final StackProps props, String stage) {
        super(parent, id, props, stage);

        Bucket fileBucket = new Bucket(this, "bfpfilebucket-" + getStage());

        UserPool userPool = UserPool.Builder.create(this, "BFPUserPool-" + getStage())
                .userPoolName("BFPUserPool-" + getStage())
                .passwordPolicy(PasswordPolicy.builder()
                        .minLength(8)
                        .requireLowercase(false)
                        .requireUppercase(false)
                        .requireDigits(false)
                        .requireSymbols(false)
                        .build())
                .accountRecovery(AccountRecovery.EMAIL_ONLY)
                .deletionProtection(true)
                .email(UserPoolEmail.withCognito())
                .signInAliases(SignInAliases.builder()
                        .username(true)
                        .email(false)
                        .build())
                .signInCaseSensitive(true)
                .selfSignUpEnabled(false)
                .mfa(Mfa.OFF)
                .standardAttributes(StandardAttributes.builder()
                        .email(StandardAttribute.builder()
                                .required(true)
                                .build())
                        .build())
                .build();

        UserPoolClient userPoolClient = UserPoolClient.Builder.create(this, "BFPUserPoolClient-" + getStage())
                .userPool(userPool)
                .authFlows(AuthFlow.builder()
                        .userPassword(true)
                        .adminUserPassword(true)
                        .build())
                .generateSecret(true)
                .build();

        Secret secret = Secret.Builder.create(this, "BFPSecret-" + getStage())
                .secretName("BFPUserPoolClientSecret-" + getStage())
                .secretName(userPoolClient.getUserPoolClientId())
                .secretStringValue(userPoolClient.getUserPoolClientSecret())
                .build();

//        DatabaseCluster postGresCluster = DatabaseCluster.Builder.create(this, "BFPDatabaseCluster-" + getStage())
//                .engine(
//
//                )
//                .defaultDatabaseName("bfp")
//                .storageEncrypted(true)
//                .build();

        PolicyDocument policyDocument = PolicyDocument.Builder.create()
                .statements(List.of(
                        PolicyStatement.Builder.create()
                                .effect(Effect.ALLOW)
                                .actions(List.of(
                                        "cognito-idp:AdminInitiateAuth",
                                        "s3:PutObject",
                                        "cognito-idp:AdminUserGlobalSignOut",
                                        "secretsmanager:GetSecretValue"
                                ))
                                .resources(List.of(
                                        userPool.getUserPoolArn(),
                                        secret.getSecretArn(),
                                        fileBucket.getBucketArn()
                                ))
                                .build())
                )
                .build();

        Role instanceRole = Role.Builder.create(this, "BFPInstanceRole-" + getStage())
                .assumedBy(new ServicePrincipal("tasks.apprunner.amazonaws.com"))
                .inlinePolicies(Map.of("BFPInstanceRolePolicy", policyDocument))
                .build();

        IRepository repository = Repository.fromRepositoryName(this, "BFPRepository-" + getStage(), "bfpservicerepository/" + getStage().toLowerCase());

        Service bfpservice = Service.Builder.create(this, "BFPService-" + getStage())
                .source(Source.fromEcr(EcrProps.builder()
                    .repository(repository)
                            .imageConfiguration(ImageConfiguration.builder()
                                    .port(8080)
                                    .environmentVariables(Map.of(
                                            "userpoolid", userPool.getUserPoolId(),
                                            "userpoolclientid", userPoolClient.getUserPoolClientId(),
                                            "spring_profiles_active", "dev",
                                            "fileBucketName", fileBucket.getBucketName()
                                    ))
                                    .build())
                        .tagOrDigest("latest")
                    .build()))
                .autoDeploymentsEnabled(true)
                .autoScalingConfiguration(AutoScalingConfiguration.Builder.create(this, "BFPAppRunnerAutoScalingConfiguration")
                        .minSize(1)
                        .maxSize(10)
                        .maxConcurrency(100)
                        .build())
                .cpu(Cpu.HALF_VCPU)
                .memory(Memory.ONE_GB)
                .healthCheck(HealthCheck.http(HttpHealthCheckOptions.builder()
                                .path("/actuator/health")
                                .timeout(Duration.seconds(5))
                                .interval(Duration.seconds(10))
                                .unhealthyThreshold(5)
                                .healthyThreshold(1)
                        .build()))
                .instanceRole(instanceRole)
                .build();

        new CfnOutput(this, "BFPUserPool-" + getStage() + " output", CfnOutputProps.builder()
                .value(userPool.getUserPoolId())
                .description("UserPoolId")
                .build());

        new CfnOutput(this, "BFPUserPoolClient-" + getStage() + " output", CfnOutputProps.builder()
                .value(userPoolClient.getUserPoolClientId())
                .description("UserPoolClientId")
                .build());

        new CfnOutput(this, "BFPServiceURL-" + getStage() + " output", CfnOutputProps.builder()
                .value(bfpservice.getServiceUrl())
                .description("BFPService URL")
                .build());
    }
}
