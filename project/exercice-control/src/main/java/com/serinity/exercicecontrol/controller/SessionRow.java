package com.serinity.exercicecontrol.controller;

import javafx.beans.property.*;

import java.time.LocalDateTime;

public class SessionRow {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> startedAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> completedAt = new SimpleObjectProperty<>();
    private final StringProperty feedback = new SimpleStringProperty();

    public SessionRow(int id, String status, LocalDateTime startedAt, LocalDateTime completedAt, String feedback) {
        this.id.set(id);
        this.status.set(status);
        this.startedAt.set(startedAt);
        this.completedAt.set(completedAt);
        this.feedback.set(feedback);
    }

    public IntegerProperty idProperty() { return id; }
    public StringProperty statusProperty() { return status; }
    public ObjectProperty<LocalDateTime> startedAtProperty() { return startedAt; }
    public ObjectProperty<LocalDateTime> completedAtProperty() { return completedAt; }
    public StringProperty feedbackProperty() { return feedback; }
}
