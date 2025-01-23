package com.bfp.cdk;

import lombok.Getter;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Tags;
import software.constructs.Construct;

@Getter
public class StagedStack extends Stack {
    private final Stage stage;

    public StagedStack(final Construct parent, final String id, final Stage stage) {
        this(parent, id, null, stage);
    }

    public StagedStack(final Construct parent, final String id, final StackProps props, Stage stage) {
        super(parent, id + "-" + stage, props);
        Tags.of(this).add("stage", stage.toString());
        this.stage = stage;
    }
}
