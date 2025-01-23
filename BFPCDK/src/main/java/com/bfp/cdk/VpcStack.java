package com.bfp.cdk;

import lombok.Getter;
import software.amazon.awscdk.NestedStack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.AmazonLinux2023ImageSsmParameterProps;
import software.amazon.awscdk.services.ec2.AmazonLinuxCpuType;
import software.amazon.awscdk.services.ec2.IKeyPair;
import software.amazon.awscdk.services.ec2.Instance;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.KeyPair;
import software.amazon.awscdk.services.ec2.MachineImage;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

import java.util.List;

@Getter
public class VpcStack extends NestedStack {
    private final Vpc vpc;
    private final SecurityGroup securityGroup;
    private final Instance bastionInstance;

    public VpcStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public VpcStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id);

        vpc = Vpc.Builder.create(this, "SimpleVpc")
                .maxAzs(2)
                .natGateways(1)
                .subnetConfiguration(List.of(
                        SubnetConfiguration.builder()
                                .cidrMask(24)
                                .name("Public")
                                .subnetType(SubnetType.PUBLIC)
                                .build(),
                        SubnetConfiguration.builder()
                                .cidrMask(24)
                                .name("Private")
                                .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                                .build()
                ))
                .build();

        securityGroup = SecurityGroup.Builder.create(this, "BFPSecurityGroup")
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();

        IKeyPair bastionKeyPair = KeyPair.fromKeyPairName(this, "BastionKeyPair", "ED25519 Pair");

        bastionInstance = Instance.Builder.create(this, "BastionHost")
                .vpc(vpc)
                .securityGroup(securityGroup)
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PUBLIC)
                        .build())
                .machineImage(MachineImage.latestAmazonLinux2023(AmazonLinux2023ImageSsmParameterProps.builder()
                        .cpuType(AmazonLinuxCpuType.ARM_64)
                        .build()
                ))
                .instanceType(InstanceType.of(InstanceClass.T4G, InstanceSize.NANO))
                .keyPair(bastionKeyPair)
                .build();
    }
}
