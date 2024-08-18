package com.bfp.cdk;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.codebuild.BuildEnvironment;
import software.amazon.awscdk.services.codebuild.BuildSpec;
import software.amazon.awscdk.services.codebuild.CloudWatchLoggingOptions;
import software.amazon.awscdk.services.codebuild.ComputeType;
import software.amazon.awscdk.services.codebuild.IProject;
import software.amazon.awscdk.services.codebuild.LinuxArmBuildImage;
import software.amazon.awscdk.services.codebuild.LoggingOptions;
import software.amazon.awscdk.services.codebuild.PipelineProject;
import software.amazon.awscdk.services.codebuild.S3LoggingOptions;
import software.amazon.awscdk.services.codepipeline.Artifact;
import software.amazon.awscdk.services.codepipeline.CfnPipeline;
import software.amazon.awscdk.services.codepipeline.ExecutionMode;
import software.amazon.awscdk.services.codepipeline.GitConfiguration;
import software.amazon.awscdk.services.codepipeline.GitPushFilter;
import software.amazon.awscdk.services.codepipeline.Pipeline;
import software.amazon.awscdk.services.codepipeline.PipelineType;
import software.amazon.awscdk.services.codepipeline.ProviderType;
import software.amazon.awscdk.services.codepipeline.StageProps;
import software.amazon.awscdk.services.codepipeline.TriggerProps;
import software.amazon.awscdk.services.codepipeline.actions.CodeBuildAction;
import software.amazon.awscdk.services.codepipeline.actions.CodeStarConnectionsSourceAction;
import software.amazon.awscdk.services.codepipeline.actions.ManualApprovalAction;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.Policy;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.LogGroupClass;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.IBucket;
import software.constructs.Construct;
import software.constructs.IConstruct;

import java.util.Collections;
import java.util.List;

public class DevOpsStack extends Stack {

    public DevOpsStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public DevOpsStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        IBucket bucket = Bucket.Builder.create(this, "CodePipelineArtifactBucket")
                .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                .build();

        Role bfpCodeBuildRole = Role.Builder.create(this, "BFPCodeBuildRole")
                .assumedBy(new ServicePrincipal("codebuild.amazonaws.com"))
                .description("CodeBuild Role for BFPBuild project")
                .build();

        Role bfpCodeDeployRole = Role.Builder.create(this, "BFPCodeDeployRole")
                .assumedBy(new ServicePrincipal("codebuild.amazonaws.com"))
                .description("CodeBuild Role for BFPDeploy project")
                .build();

        LogGroup bfpBuildLogGroup = LogGroup.Builder.create(this, "BFPBuildLogGroup")
                .logGroupName("/aws/codebuild/BFPBuild")
                .logGroupClass(LogGroupClass.STANDARD)
                .retention(RetentionDays.ONE_WEEK)
                .build();

        LogGroup bfpDeployLogGroup = LogGroup.Builder.create(this, "BFPDeployLogGroup")
                .logGroupName("/aws/codebuild/BFPDeploy")
                .logGroupClass(LogGroupClass.STANDARD)
                .retention(RetentionDays.ONE_WEEK)
                .build();

        BuildEnvironment standardBuildEnvironment = BuildEnvironment.builder()
                .buildImage(LinuxArmBuildImage.AMAZON_LINUX_2_STANDARD_3_0)
                .computeType(ComputeType.SMALL)
                .build();

        IProject buildProject = PipelineProject.Builder.create(this, "BFPBuildProject")
                .projectName("BFPBuild")
                .buildSpec(BuildSpec.fromSourceFilename("buildfiles/build.yaml"))
                .logging(LoggingOptions.builder()
                        .cloudWatch(CloudWatchLoggingOptions.builder()
                                .enabled(true)
                                .logGroup(bfpBuildLogGroup)
                                .build())
//                        .s3(S3LoggingOptions.builder()
//                                .enabled(false)
//                                .build())
                        .build())
                .role(bfpCodeBuildRole)
                .environment(standardBuildEnvironment)
                .build();

        IProject deployProject = PipelineProject.Builder.create(this, "BFPDeployProject")
                .projectName("BFPDeploy")
                .buildSpec(BuildSpec.fromSourceFilename("buildfiles/build and deploy to ecr.yaml"))
                .logging(LoggingOptions.builder()
                        .cloudWatch(CloudWatchLoggingOptions.builder()
                                .enabled(true)
                                .logGroup(bfpDeployLogGroup)
                                .build())
//                        .s3(S3LoggingOptions.builder()
//                                .enabled(false)
//                                .build())
                        .build())
                .role(bfpCodeDeployRole)
                .environment(standardBuildEnvironment)
                .build();

        Pipeline pipeline = Pipeline.Builder.create(this, "DevOpsPipeline")
                .pipelineName("DevOpsPipeline")
                .artifactBucket(bucket)
                .pipelineType(PipelineType.V2)
                .executionMode(ExecutionMode.QUEUED)
                .restartExecutionOnUpdate(true)
                .role(Role.fromRoleArn(this, "CodePipelineRole", "arn:aws:iam::891377256793:role/service-role/AWSCodePipelineServiceRole-us-east-1-BFP"))
                .triggers(List.of(
                        TriggerProps.builder()
                                .providerType(ProviderType.CODE_STAR_SOURCE_CONNECTION)
                                .gitConfiguration(GitConfiguration.builder()
                                        .sourceAction(CodeStarConnectionsSourceAction.Builder.create()
                                                .branch("master")
                                                .repo("MyYogurt/bfp")
                                                .triggerOnPush(true)
                                                .connectionArn("arn:aws:codeconnections:us-east-1:891377256793:connection/b090eeb1-9ef7-4a27-929e-6fc6e4607648")
                                                .actionName("Source")
                                                .output(Artifact.artifact("SourceArtifact"))
                                                .owner("AWS")
                                                .build())
                                        .pushFilter(List.of(
                                                GitPushFilter.builder()
                                                        .tagsExcludes(Collections.emptyList())
                                                        .build()
                                        ))
                                        .build())
                                .build()))
                .stages(List.of(
                        StageProps.builder()
                                .stageName("Source")
                                .actions(List.of(
                                        CodeStarConnectionsSourceAction.Builder.create()
                                                .branch("master")
                                                .repo("MyYogurt/bfp")
                                                .connectionArn("arn:aws:codeconnections:us-east-1:891377256793:connection/b090eeb1-9ef7-4a27-929e-6fc6e4607648")
                                                .actionName("Source")
                                                .codeBuildCloneOutput(true)
                                                .output(Artifact.artifact("SourceArtifact"))
                                                .owner("AWS")
                                                .build()
                                ))
                                .build(),
                        StageProps.builder()
                                .stageName("Build")
                                .actions(List.of(
                                        CodeBuildAction.Builder.create()
                                                .actionName("Build")
                                                .project(buildProject)
                                                .input(Artifact.artifact("SourceArtifact"))
                                                .outputs(List.of(Artifact.artifact("BuildArtifact")))
                                                .build()
                                ))
                                .build(),
                        StageProps.builder()
                                .stageName("Manual-Approve")
                                .actions(List.of(
                                        ManualApprovalAction.Builder.create()
                                                .actionName("Approve-release")
                                                .build()
                                ))
                                .build(),
                        StageProps.builder()
                                .stageName("Deploy")
                                .actions(List.of(
                                        CodeBuildAction.Builder.create()
                                                .actionName("Deploy")
                                                .project(deployProject)
                                                .input(Artifact.artifact("SourceArtifact"))
                                                .build()
                                ))
                                .build()
                ))
                .build();

        Policy baseCodeBuildPolicy = Policy.Builder.create(this, "BaseCodeBuildPolicy")
                .statements(List.of(
                        PolicyStatement.Builder.create()
                                .effect(Effect.ALLOW)
                                .resources(List.of(
                                        bfpBuildLogGroup.getLogGroupArn(),
                                        bfpBuildLogGroup.getLogGroupArn() + ":*",
                                        bfpDeployLogGroup.getLogGroupArn(),
                                        bfpDeployLogGroup.getLogGroupArn() + ":*"
                                ))
                                .actions(List.of(
                                        "logs:CreateLogGroup",
                                        "logs:CreateLogStream",
                                        "logs:PutLogEvents"
                                ))
                                .build(),
                        PolicyStatement.Builder.create()
                                .effect(Effect.ALLOW)
                                .resources(List.of(
                                        bucket.getBucketArn()
                                ))
                                .actions(List.of(
                                        "s3:PutObject",
                                        "s3:GetObject",
                                        "s3:GetObjectVersion",
                                        "s3:GetBucketAcl",
                                        "s3:GetBucketLocation"
                                ))
                                .build(),
                        PolicyStatement.Builder.create()
                                .effect(Effect.ALLOW)
                                .resources(List.of(
                                        buildProject.getProjectArn(),
                                        deployProject.getProjectArn()
                                ))
                                .actions(List.of(
                                        "codebuild:CreateReportGroup",
                                        "codebuild:CreateReport",
                                        "codebuild:UpdateReport",
                                        "codebuild:BatchPutTestCases",
                                        "codebuild:BatchPutCodeCoverages"
                                ))
                                .build()
                ))
                .roles(List.of(
                        bfpCodeBuildRole,
                        bfpCodeDeployRole
                ))
                .build();

        Policy deployToEcrPolicy = Policy.Builder.create(this, "CodeBuildDeployToEcr")
                .statements(List.of(
                        PolicyStatement.Builder.create()
                                .effect(Effect.ALLOW)
                                .resources(List.of(
                                        Repository.fromRepositoryName(this, "BFPRepository", "bfprepository").getRepositoryArn()
                                ))
                                .actions(List.of(
                                        "ecr:CompleteLayerUpload",
                                        "ecr:UploadLayerPart",
                                        "ecr:InitiateLayerUpload",
                                        "ecr:BatchCheckLayerAvailability",
                                        "ecr:PutImage"
                                ))
                                .build(),
                        PolicyStatement.Builder.create()
                                .effect(Effect.ALLOW)
                                .resources(List.of(
                                        "*"
                                ))
                                .actions(List.of(
                                        "ecr:GetAuthorizationToken"
                                ))
                                .build()
                ))
                .roles(List.of(
                        bfpCodeDeployRole
                ))
                .build();
    }
}
