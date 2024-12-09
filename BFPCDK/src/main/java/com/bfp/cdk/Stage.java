package com.bfp.cdk;

public enum Stage {
    Gamma("Gamma"),
    Prod("Prod");

    private final String stage;

    Stage(String stage) {
        this.stage = stage;
    }

    @Override
    public String toString() {
        return getStage();
    }

    public String getStage() {
        return stage.toLowerCase();
    }

    public static Stage fromString(String text) {
        for (Stage stage : Stage.values()) {
            if (stage.stage.equalsIgnoreCase(text)) {
                return stage;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
