package com.bfp.cdk;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
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
import software.amazon.awscdk.services.apprunner.alpha.VpcConnector;
import software.amazon.awscdk.services.apprunner.alpha.VpcConnectorProps;
import software.amazon.awscdk.services.cognito.AccountRecovery;
import software.amazon.awscdk.services.cognito.AuthFlow;
import software.amazon.awscdk.services.cognito.CognitoDomainOptions;
import software.amazon.awscdk.services.cognito.Mfa;
import software.amazon.awscdk.services.cognito.PasswordPolicy;
import software.amazon.awscdk.services.cognito.SignInAliases;
import software.amazon.awscdk.services.cognito.StandardAttribute;
import software.amazon.awscdk.services.cognito.StandardAttributes;
import software.amazon.awscdk.services.cognito.UserPool;
import software.amazon.awscdk.services.cognito.UserPoolClient;
import software.amazon.awscdk.services.cognito.UserPoolDomain;
import software.amazon.awscdk.services.cognito.UserPoolEmail;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyDocument;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.rds.DatabaseInstance;
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine;
import software.amazon.awscdk.services.rds.PostgresEngineVersion;
import software.amazon.awscdk.services.rds.PostgresInstanceEngineProps;
import software.amazon.awscdk.services.rds.SubnetGroup;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.secretsmanager.Secret;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class BFPServiceStack extends StagedStack {
    public BFPServiceStack(final Construct parent, final String id, Stage stage) {
        this(parent, id, null, stage);
    }

    public BFPServiceStack(final Construct parent, final String id, final StackProps props, Stage stage) {
        super(parent, id, props, stage);

        Bucket fileBucket = createFileBucket();

        UserPool userPool = createUserPool();
        UserPoolClient userPoolClient = createUserPoolClient(userPool);
        UserPoolDomain userPoolDomain = createUserPoolDomain(userPool);
        Secret userPoolClientSecret = createUserPoolClientSecret(userPoolClient);

        if (getStage().equals(Stage.Dev)) {
            return;
        }

        VpcStack vpc = createVpc();
        DatabaseInstance database = createDatabase(vpc);
        createService(vpc, userPool, userPoolClient, userPoolClientSecret, fileBucket, database);
    }

    VpcStack createVpc() {
        VpcStack vpc = new VpcStack(this, "BFPVpcStack");

        new CfnOutput(this, "BastionHostIp-" + getStage(), CfnOutputProps.builder()
                .value(vpc.getBastionInstance().getInstancePublicIp())
                .description("Public IP of bastion host")
                .build()
        );

        return vpc;
    }

    Bucket createFileBucket() {
        Bucket fileBucket = new Bucket(this, "bfpfilebucket");
        fileBucket.applyRemovalPolicy(RemovalPolicy.DESTROY);

        new CfnOutput(this, "BFPFileBucket-" + getStage(), CfnOutputProps.builder()
                .value(fileBucket.getBucketName())
                .description("File bucket name")
                .build());

        return fileBucket;
    }

    DatabaseInstance createDatabase(VpcStack vpc) {
        SubnetGroup subnetGroup = SubnetGroup.Builder.create(this, "BFPSubnetGroup")
                .vpc(vpc.getVpc())
                .description("BFPSubnetGroup")
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                        .build())
                .build();

        SecurityGroup rdsSecurityGroup = SecurityGroup.Builder.create(this, "BFPDatabaseSecurityGroup")
                .vpc(vpc.getVpc())
                .build();
        rdsSecurityGroup.addIngressRule(vpc.getSecurityGroup(), Port.POSTGRES, "Allow postgres traffic.");
        rdsSecurityGroup.addEgressRule(vpc.getSecurityGroup(), Port.POSTGRES, "Allow postgres traffic.");

        DatabaseInstance postgresInstance = DatabaseInstance.Builder.create(this, "BFPDatabaseInstance")
                .engine(DatabaseInstanceEngine.postgres(PostgresInstanceEngineProps.builder()
                        .version(PostgresEngineVersion.VER_17_2)
                        .build()))
                .instanceType(InstanceType.of(InstanceClass.T4G, InstanceSize.MICRO))
                .vpc(vpc.getVpc())
                .securityGroups(List.of(rdsSecurityGroup))
                .subnetGroup(subnetGroup)
                .enablePerformanceInsights(false)
                .databaseName("postgres")
                .backupRetention(Duration.days(0))
                .autoMinorVersionUpgrade(true)
                .multiAz(false)
                .build();
        return postgresInstance;
    }

    UserPool createUserPool() {
        UserPool userPool = UserPool.Builder.create(this, "BFPUserPool")
                .passwordPolicy(PasswordPolicy.builder()
                        .minLength(8)
                        .requireLowercase(false)
                        .requireUppercase(false)
                        .requireDigits(false)
                        .requireSymbols(false)
                        .build())
                .accountRecovery(AccountRecovery.NONE)
                .deletionProtection(false)
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
        userPool.applyRemovalPolicy(RemovalPolicy.DESTROY);

        new CfnOutput(this, "BFPUserPool-" + getStage() + " output", CfnOutputProps.builder()
                .value(userPool.getUserPoolId())
                .description("UserPoolId")
                .build());

        return userPool;
    }

    UserPoolClient createUserPoolClient(UserPool userPool) {
        UserPoolClient userPoolClient = UserPoolClient.Builder.create(this, "BFPUserPoolClient")
                .userPool(userPool)
                .authFlows(AuthFlow.builder()
                        .userPassword(true)
                        .adminUserPassword(true)
                        .build())
                .generateSecret(true)
                .build();

        new CfnOutput(this, "BFPUserPoolClient-" + getStage() + " output", CfnOutputProps.builder()
                .value(userPoolClient.getUserPoolClientId())
                .description("UserPoolClientId")
                .build());

        return userPoolClient;
    }

    Secret createUserPoolClientSecret(UserPoolClient userPoolClient) {
        Secret secret = Secret.Builder.create(this, "BFPSecret")
                .secretName("BFPUserPoolClientSecret-" + getStage())
                .secretName(userPoolClient.getUserPoolClientId())
                .secretStringValue(userPoolClient.getUserPoolClientSecret())
                .build();
        return secret;
    }

    UserPoolDomain createUserPoolDomain(UserPool userPool) {
        UserPoolDomain domain = UserPoolDomain.Builder.create(this, "BFPUserPoolDomain")
                .userPool(userPool)
                .cognitoDomain(CognitoDomainOptions.builder()
                        .domainPrefix("bfp-" + getStage())
                        .build())
                .build();

        new CfnOutput(this, "BFPUserPoolDomain-" + getStage() + " output", CfnOutputProps.builder()
                .value(domain.getDomainName())
                .description("UserPoolDomain")
                .build());

        return domain;
    }

    Service createService(VpcStack vpc, UserPool userPool, UserPoolClient userPoolClient, Secret userPoolClientSecret, Bucket fileBucket, DatabaseInstance postgresInstance) {
        PolicyDocument policyDocument = PolicyDocument.Builder.create()
                .statements(List.of(
                        PolicyStatement.Builder.create()
                                .effect(Effect.ALLOW)
                                .actions(List.of(
                                        "cognito-idp:AdminInitiateAuth",
                                        "cognito-idp:AdminUserGlobalSignOut"
                                ))
                                .resources(List.of(
                                        userPool.getUserPoolArn()
                                ))
                                .build(),
                        PolicyStatement.Builder.create()
                                .effect(Effect.ALLOW)
                                .actions(List.of(
                                        "s3:PutObject",
                                        "s3:GetObject",
                                        "s3:DeleteObject"
                                ))
                                .resources(List.of(
                                        fileBucket.getBucketArn(),
                                        fileBucket.getBucketArn() + "/*"
                                ))
                                .build(),
                        PolicyStatement.Builder.create()
                                .effect(Effect.ALLOW)
                                .actions(List.of(
                                        "secretsmanager:GetSecretValue"
                                ))
                                .resources(List.of(
                                        userPoolClientSecret.getSecretArn(),
                                        postgresInstance.getSecret().getSecretArn()
                                ))
                                .build())
                )
                .build();

        Role instanceRole = Role.Builder.create(this, "BFPInstanceRole")
                .assumedBy(new ServicePrincipal("tasks.apprunner.amazonaws.com"))
                .inlinePolicies(Map.of("BFPInstanceRolePolicy", policyDocument))
                .build();

        IRepository repository = Repository.fromRepositoryName(this, "BFPRepository", "bfpservicerepository/" + getStage().toString().toLowerCase());

        VpcConnector vpcConnector = new VpcConnector(this, "BFPVpcConnector", VpcConnectorProps.builder()
                .vpc(vpc.getVpc())
                .securityGroups(List.of(vpc.getSecurityGroup()))
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                        .build())
                .build());

        Service bfpservice = Service.Builder.create(this, "BFPService")
                .source(Source.fromEcr(EcrProps.builder()
                        .repository(repository)
                        .imageConfiguration(ImageConfiguration.builder()
                                .port(8080)
                                .environmentVariables(Map.of(
                                        "userpoolid", userPool.getUserPoolId(),
                                        "userpoolclientid", userPoolClient.getUserPoolClientId(),
                                        "fileBucketName", fileBucket.getBucketName(),
                                        "postgreshost", postgresInstance.getDbInstanceEndpointAddress(),
                                        "postgresusername", postgresInstance.getSecret().secretValueFromJson("username").toString(),
                                        "postgrespassword", postgresInstance.getSecret().secretValueFromJson("password").toString()
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
                .vpcConnector(vpcConnector)
                .instanceRole(instanceRole)
                .build();

        new CfnOutput(this, "BFPServiceURL-" + getStage() + " output", CfnOutputProps.builder()
                .value(bfpservice.getServiceUrl())
                .description("BFPService URL")
                .build());

        return bfpservice;
    }
}
