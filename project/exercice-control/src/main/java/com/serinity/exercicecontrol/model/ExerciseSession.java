package com.serinity.exercicecontrol.model;

import java.time.LocalDateTime;

public class ExerciseSession {

    private int id;
    private int userId;
    private int exerciseId;
    private String status; // STARTED, COMPLETED, CANCELLED, etc.

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    // Optionnel : feedback JSON (reps, rpe, pain, etc.)
    private String feedback;

    // ===================== CONSTRUCTEURS =====================

    public ExerciseSession() {
    }

    public ExerciseSession(int userId, int exerciseId, String status) {
        this.userId = userId;
        this.exerciseId = exerciseId;
        this.status = status;
        this.startedAt = LocalDateTime.now();
    }

    public ExerciseSession(int id, int userId, int exerciseId,
                           String status,
                           LocalDateTime startedAt,
                           LocalDateTime completedAt,
                           String feedback) {
        this.id = id;
        this.userId = userId;
        this.exerciseId = exerciseId;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.feedback = feedback;
    }

    // ===================== GETTERS =====================

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getExerciseId() {
        return exerciseId;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public String getFeedback() {
        return feedback;
    }

    // ===================== SETTERS =====================

    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    // ===================== LOGIQUE UTILE =====================

    public boolean isCompleted() {
        return "COMPLETED".equalsIgnoreCase(status);
    }

    public long getDurationSeconds() {
        if (startedAt == null || completedAt == null) return 0;
        return java.time.Duration.between(startedAt, completedAt).getSeconds();
    }

    @Override
    public String toString() {
        return "ExerciseSession{" +
                "id=" + id +
                ", userId=" + userId +
                ", exerciseId=" + exerciseId +
                ", status='" + status + '\'' +
                ", startedAt=" + startedAt +
                ", completedAt=" + completedAt +
                ", feedback='" + feedback + '\'' +
                '}';
    }
}