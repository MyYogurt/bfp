package com.bfp.cdk;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.Protocol;
import software.constructs.Construct;

import java.util.List;

public class BFPServiceStatefulStack extends Stack {
    public BFPServiceStatefulStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public BFPServiceStatefulStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        // Create VPC with a AZ limit of two.
        Vpc vpc = Vpc.Builder.create(this, "BFPVPC")
                .maxAzs(2)
                .build();

        // Create the ECS Service
        Cluster cluster = Cluster.Builder.create(this, "BFPCluster")
                .vpc(vpc)
                .build();

        IRepository ecrRepo = Repository.fromRepositoryName(this, "bfp", "bfpservicerepository");

        // Use the ECS Network Load Balanced Fargate Service construct to create a ECS service
        ApplicationLoadBalancedFargateService fargateService = ApplicationLoadBalancedFargateService.Builder.create(this, "MyFargateService")
                .cluster(cluster)
                .cpu(256)
                .memoryLimitMiB(512)
                .desiredCount(1)
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .image(ContainerImage.fromEcrRepository(ecrRepo, "latest"))
                                .containerPort(8080)
                                .build())
                .healthCheck(software.amazon.awscdk.services.ecs.HealthCheck.builder()
                        .command(List.of("CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"))
                        .build())
                .publicLoadBalancer(true)
                .build();

        fargateService.getTargetGroup().configureHealthCheck(software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck.builder()
                .path("/actuator/health")
                .healthyHttpCodes("200")
                .protocol(Protocol.HTTP)
                .port("8080")
                .interval(Duration.seconds(30))
                .build());

        new CfnOutput(this, "LoadBalancerDNS", CfnOutputProps.builder()
                .value(fargateService.getLoadBalancer().getLoadBalancerDnsName())
                .description("Load balancer DNS")
                .build());
    }
}
