package com.bfp.cdk;

import lombok.Getter;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Tags;
import software.constructs.Construct;

@Getter
public class StagedStack extends Stack {
    private final String stage;

    public StagedStack(final Construct parent, final String id, final String stage) {
        this(parent, id, null, stage);
    }

    public StagedStack(final Construct parent, final String id, final StackProps props, String stage) {
        super(parent, id + "-" + stage, props);
        Tags.of(this).add("stage", stage);
        this.stage = stage;
    }
}
