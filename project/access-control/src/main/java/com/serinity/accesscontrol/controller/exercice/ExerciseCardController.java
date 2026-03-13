package com.serinity.accesscontrol.controller.exercice;

import com.serinity.accesscontrol.model.exercice.Exercise;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ExerciseCardController {

    @FXML private Label lblTitle;
    @FXML private Label lblBadge;
    @FXML private Label lblType;
    @FXML private Label lblDuration;

    private Exercise exercise;
    private ExerciseListController parent;

    public void setData(Exercise ex, ExerciseListController parent) {
        this.exercise = ex;
        this.parent = parent;

        lblTitle.setText(safe(ex.getTitle()));
        lblType.setText("Type : " + safe(ex.getType()));
        lblDuration.setText("Durée : " + ex.getDurationMinutes() + " min");
        lblBadge.setText("Niveau " + ex.getLevel());
    }

    @FXML
    private void onOpenDetails() {
        if (parent != null && exercise != null) parent.openDetails(exercise);
    }

    @FXML
    private void onEdit() {
        if (parent != null && exercise != null) parent.openEdit(exercise);
    }

    @FXML
    private void onDelete() {
        if (parent != null && exercise != null) parent.deleteExercise(exercise);
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}