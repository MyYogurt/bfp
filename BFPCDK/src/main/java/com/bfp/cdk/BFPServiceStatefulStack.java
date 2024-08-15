package com.bfp.cdk;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.apprunner.alpha.AutoScalingConfiguration;
import software.amazon.awscdk.services.apprunner.alpha.Cpu;
import software.amazon.awscdk.services.apprunner.alpha.EcrProps;
import software.amazon.awscdk.services.apprunner.alpha.HealthCheck;
import software.amazon.awscdk.services.apprunner.alpha.HttpHealthCheckOptions;
import software.amazon.awscdk.services.apprunner.alpha.ImageConfiguration;
import software.amazon.awscdk.services.apprunner.alpha.Memory;
import software.amazon.awscdk.services.apprunner.alpha.Service;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apprunner.alpha.Source;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.constructs.Construct;

public class BFPServiceStatefulStack extends Stack {
    public BFPServiceStatefulStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public BFPServiceStatefulStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        IRepository repository = Repository.fromRepositoryName(this, "BFPRepository", "bfprepository");

        Service bfpservice = Service.Builder.create(this, "BFPService")
                .serviceName("BFPService")
                .source(Source.fromEcr(EcrProps.builder()
                    .repository(repository)
                            .imageConfiguration(ImageConfiguration.builder()
                                    .port(8080)
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
                .build();

        new CfnOutput(this, "BFPServiceURL", CfnOutputProps.builder()
                .value(bfpservice.getServiceUrl())
                .description("BFPService URL")
                .build());
    }
}
