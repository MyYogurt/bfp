package com.bfp.cdk;

import software.amazon.awscdk.App;

public class BFPApp {
    public static void main(String[] args) {
        App app = new App();
        new BFPServiceStatefulStack(app, "BFPService");
//        new DevOpsStack(app, "BFPDevOps");
        app.synth();
    }
}
