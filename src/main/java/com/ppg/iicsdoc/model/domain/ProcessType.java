package com.ppg.iicsdoc.model.domain;

public enum ProcessType {
    CAI("Cloud Application Integration"),
    CDI("Cloud Data Integration");

    private final String displayName;

    ProcessType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}
