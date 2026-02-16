package com.serinity.forumcontrol.Models;

public enum ThreadStatus {
    OPEN("open"),
    LOCKED("locked"),
    ARCHIVED("archived"),
    HIDDEN("hidden");

    private final String value;

    ThreadStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ThreadStatus fromString(String value) {
        for (ThreadStatus status : ThreadStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return OPEN;
    }
}