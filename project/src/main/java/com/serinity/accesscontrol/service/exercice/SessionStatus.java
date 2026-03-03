package com.serinity.accesscontrol.service.exercice;

public enum SessionStatus {
    CREATED,
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
    ABORTED,
    CANCELLED;

    public static SessionStatus fromDb(String s) {
        if (s == null) return CREATED;
        return SessionStatus.valueOf(s.trim().toUpperCase());
    }
}