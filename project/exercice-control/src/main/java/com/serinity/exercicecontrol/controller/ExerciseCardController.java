package com.serinity.exercicecontrol.controller;

import com.serinity.exercicecontrol.model.Exercise;
import com.serinity.exercicecontrol.security.AuthContext;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ExerciseCardController {

    @FXML private Label lblTitle;
    @FXML private Label lblBadge;
    @FXML private Label lblType;
    @FXML private Label lblDuration;

    // ✅ AJOUT: boutons admin-only (doivent exister dans ExerciseCard.fxml)
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private Exercise exercise;
    private ExerciseListController parent;

    public void setData(Exercise ex, ExerciseListController parent) {
        this.exercise = ex;
        this.parent = parent;

        lblTitle.setText(safe(ex.getTitle()));
        lblType.setText("Type : " + safe(ex.getType()));
        lblDuration.setText("Durée : " + ex.getDurationMinutes() + " min");
        lblBadge.setText("Niveau " + ex.getLevel());

        // ✅ ADMIN: cacher Modifier/Supprimer si pas admin
        boolean admin = AuthContext.isAdmin();

        if (btnEdit != null) {
            btnEdit.setVisible(admin);
            btnEdit.setManaged(admin);
            btnEdit.setDisable(!admin);
        }
        if (btnDelete != null) {
            btnDelete.setVisible(admin);
            btnDelete.setManaged(admin);
            btnDelete.setDisable(!admin);
        }
    }

    @FXML
    private void onOpenDetails() {
        if (parent != null && exercise != null) parent.openDetails(exercise);
    }

    @FXML
    private void onEdit() {
        if (!AuthContext.isAdmin()) return;
        if (parent != null && exercise != null) parent.openEdit(exercise);
    }

    @FXML
    private void onDelete() {
        if (!AuthContext.isAdmin()) return;
        if (parent != null && exercise != null) parent.deleteExercise(exercise);
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}