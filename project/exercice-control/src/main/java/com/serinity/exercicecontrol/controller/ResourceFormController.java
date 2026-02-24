package com.serinity.exercicecontrol.controller;

import com.serinity.exercicecontrol.model.Exercise;
import com.serinity.exercicecontrol.model.Resource;
import com.serinity.exercicecontrol.service.ResourceService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.sql.SQLException;

public class ResourceFormController {

    @FXML private Label lblHeader;
    @FXML private TextField txtTitle;
    @FXML private ComboBox<String> cbMediaType;
    @FXML private TextField txtUrl;
    @FXML private TextArea txtContent;
    @FXML private TextField txtDurationSeconds;
    @FXML private Button btnSave;
    @FXML private Button btnBack;

    private final ResourceService resourceService = new ResourceService();

    private Exercise returnExercise;       // pour revenir à Details
    private Resource editing;              // null => create, sinon edit
    private Runnable onDoneRefresh;        // callback après save

    @FXML
    public void initialize() {
        cbMediaType.setItems(FXCollections.observableArrayList("VIDEO", "AUDIO", "TEXTE"));
        cbMediaType.getSelectionModel().selectFirst();
    }



    public void setModeCreateReturnToDetails(Exercise ex, Runnable onDoneRefresh) {
        this.returnExercise = ex;
        this.onDoneRefresh = onDoneRefresh;
        this.editing = null;

        lblHeader.setText("Ajouter une ressource");
        btnSave.setText("Ajouter");
        clearFields();
    }

    public void setModeEditReturnToDetails(Exercise ex, Resource r, Runnable onDoneRefresh) {
        this.returnExercise = ex;
        this.onDoneRefresh = onDoneRefresh;
        this.editing = r;

        lblHeader.setText("Modifier la ressource");
        btnSave.setText("Enregistrer");

        txtTitle.setText(nvl(r.getTitle()));
        cbMediaType.setValue(r.getMediaType() == null ? null : r.getMediaType().toUpperCase());
        txtUrl.setText(nvl(r.getUrl()));
        txtContent.setText(nvl(r.getContent()));
        txtDurationSeconds.setText(String.valueOf(r.getDurationSeconds()));
    }



    @FXML
    private void onSave() {
        try {
            if (returnExercise == null) {
                showError("Erreur", "Exercice de retour introuvable (returnExercise=null).");
                return;
            }

            Resource r = new Resource();
            if (editing != null) r.setId(editing.getId());

            r.setExerciseId(returnExercise.getId());
            r.setTitle(txtTitle.getText());
            r.setMediaType(cbMediaType.getValue());


            r.setUrl(txtUrl.getText());
            r.setContent(txtContent.getText());

            int dur = 0;
            if (txtDurationSeconds.getText() != null && !txtDurationSeconds.getText().isBlank()) {
                dur = Integer.parseInt(txtDurationSeconds.getText().trim());
            }
            r.setDurationSeconds(dur);

            if (editing == null) {
                resourceService.addResource(r);
                showInfo("Ajout", "Ressource ajoutée avec succès.");
            } else {
                resourceService.updateResource(r);
                showInfo("Mise à jour", "Ressource modifiée avec succès.");
            }

            if (onDoneRefresh != null) onDoneRefresh.run();
            goBackToDetails();

        } catch (NumberFormatException e) {
            showError("Erreur", "Durée invalide. Mets un nombre (secondes).");
        } catch (IllegalArgumentException e) {
            showError("Erreur validation", e.getMessage());
        } catch (SQLException e) {
            showError("Erreur BD", e.getMessage());
        }
    }

    @FXML
    private void onBack() {
        goBackToDetails();
    }

    // ========= Navigation =========

    private void goBackToDetails() {
        try {
            if (returnExercise == null) return;

            javafx.fxml.FXMLLoader loader =
                    new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/exercice/ExerciseDetails.fxml"));
            Parent page = loader.load();

            ExerciseDetailsController ctrl = loader.getController();
            ctrl.setExercise(returnExercise);

            setContent(page);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de retourner aux détails.");
        }
    }

    private void setContent(Parent page) {
        StackPane host = (StackPane) btnSave.getScene().lookup("#contentHost");
        if (host == null) {
            throw new IllegalStateException("contentHost introuvable. Vérifie fx:id=\"contentHost\" dans Template.fxml");
        }
        host.getChildren().setAll(page);
    }

    // ========= Helpers =========

    private void clearFields() {
        txtTitle.clear();
        cbMediaType.getSelectionModel().selectFirst();
        txtUrl.clear();
        txtContent.clear();
        txtDurationSeconds.clear();
    }

    private String nvl(String s) { return s == null ? "" : s; }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
