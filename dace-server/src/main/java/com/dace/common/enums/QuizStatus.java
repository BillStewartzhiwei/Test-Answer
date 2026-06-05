package com.dace.common.enums;

public enum QuizStatus {
    DRAFT(1),
    PUBLISHED(2),
    CLOSED(3);

    private final int value;

    QuizStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
