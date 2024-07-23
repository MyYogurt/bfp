package com.bfp.cdk;

import software.amazon.awscdk.App;

public class BFPApp {
    public static void main(String[] args) {
        App app = new App();
        new StatefulStack(app, "test");
        app.synth();
    }
}
