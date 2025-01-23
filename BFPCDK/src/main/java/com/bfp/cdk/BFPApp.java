package com.bfp.cdk;

import software.amazon.awscdk.App;

public class BFPApp {
    public static void main(String[] args) {
        App app = new App();
        String stage = (String) app.getNode().tryGetContext("stage");
        Stage stageEnum = Stage.fromString(stage);
        BFPServiceStack serviceStack = new BFPServiceStack(app, "BFPService", stageEnum);
        app.synth();
    }
}
