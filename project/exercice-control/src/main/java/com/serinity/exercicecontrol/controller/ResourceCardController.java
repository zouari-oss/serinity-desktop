package com.serinity.exercicecontrol.controller;

import com.serinity.exercicecontrol.model.Resource;
import com.serinity.exercicecontrol.security.AuthContext;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ResourceCardController {

    @FXML private Label lblTitle;
    @FXML private Label lblMeta;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private Resource resource;
    private ExerciseDetailsController parent;

    public void setData(Resource r, ExerciseDetailsController parent) {
        this.resource = r;
        this.parent = parent;

        lblTitle.setText(r != null && r.getTitle() != null ? r.getTitle() : "(Sans titre)");

        String type = (r != null && r.getMediaType() != null) ? r.getMediaType() : "-";
        int dur = (r != null) ? r.getDurationSeconds() : 0;

        lblMeta.setText("Type : " + type + " • Durée : " + dur + " s");

        // ✅ ADMIN: cacher les boutons si pas admin
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
    private void onEdit() {
        if (!AuthContext.isAdmin()) return;
        if (parent != null && resource != null) {
            parent.openEditResource(resource);
        }
    }

    @FXML
    private void onDelete() {
        if (!AuthContext.isAdmin()) return;
        if (parent != null && resource != null) {
            parent.deleteResource(resource);
        }
    }
}