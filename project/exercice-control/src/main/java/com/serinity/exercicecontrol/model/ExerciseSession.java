package com.serinity.exercicecontrol.model;

import java.time.LocalDateTime;

public class ExerciseSession {
    private int id;
    private int userId;
    private int exerciseId;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String feedback;

    public ExerciseSession() {}

    public ExerciseSession(int id, int userId, int exerciseId, String status,
                           LocalDateTime startedAt, LocalDateTime completedAt, String feedback) {
        this.id = id;
        this.userId = userId;
        this.exerciseId = exerciseId;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.feedback = feedback;
    }

    public ExerciseSession(int userId, int exerciseId, String status,
                           LocalDateTime startedAt, LocalDateTime completedAt, String feedback) {
        this.userId = userId;
        this.exerciseId = exerciseId;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.feedback = feedback;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getExerciseId() { return exerciseId; }
    public void setExerciseId(int exerciseId) { this.exerciseId = exerciseId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    @Override
    public String toString() {
        return "ExerciseSession{" +
                "id=" + id +
                ", userId=" + userId +
                ", exerciseId=" + exerciseId +
                ", status='" + status + '\'' +
                ", startedAt=" + startedAt +
                ", completedAt=" + completedAt +
                '}';
    }
}
