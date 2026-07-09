package com.hitanshudhawan.annotationprocessingexample.network;

public enum PriorityLevel {

    PRIORITY_TYPE_LOW(1),
    PRIORITY_TYPE_NORMAL(2),
    PRIORITY_TYPE_HIGH(3);

    private final int value;

    PriorityLevel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PriorityLevel valueOf(int state) {
        for (PriorityLevel policy : values()) {
            if (policy.getValue() == state) {
                return policy;
            }
        }
        return PriorityLevel.PRIORITY_TYPE_NORMAL;
    }

}
