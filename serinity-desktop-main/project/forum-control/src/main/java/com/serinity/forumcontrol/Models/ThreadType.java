package com.serinity.forumcontrol.Models;

public enum ThreadType {
    DISCUSSION("discussion"),
    QUESTION("question"),
    ANNOUNCEMENT("announcement");

    private final String value;

    ThreadType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ThreadType fromString(String value) {
        for (ThreadType type : ThreadType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return DISCUSSION;
    }
}