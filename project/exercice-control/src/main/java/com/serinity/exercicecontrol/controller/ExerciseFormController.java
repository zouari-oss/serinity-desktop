package com.serinity.exercicecontrol.controller;

import com.serinity.exercicecontrol.model.Exercise;
import com.serinity.exercicecontrol.service.ExerciseService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.sql.SQLException;

public class ExerciseFormController {

    @FXML private Label lblHeader;

    @FXML private TextField txtTitle;
    @FXML private ComboBox<String> cbType;
    @FXML private ComboBox<Integer> cbLevel;

    // IMPORTANT: must match ExerciseForm.fxml fx:id="txtDuration"
    @FXML private TextField txtDuration;

    @FXML private TextArea txtDescription;

    @FXML private Button btnSave;

    private final ExerciseService exerciseService = new ExerciseService();

    private Exercise editing;          // null => create
    private Runnable onDoneRefresh;    // callback after save (ex: refresh list)

    @FXML
    public void initialize() {
        cbType.setItems(FXCollections.observableArrayList(
                "respiration", "méditation", "relaxation", "yoga", "plein_conscience"
        ));

        cbLevel.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        cbLevel.setValue(1);
    }

    // =============================
    // MODES
    // =============================

    /** Mode création + retour liste */
    public void setModeCreateReturnToList(Runnable onDoneRefresh) {
        this.editing = null;
        this.onDoneRefresh = onDoneRefresh;

        lblHeader.setText("Ajouter un exercice");
        btnSave.setText("Ajouter");
        clearFields();
    }

    /** Mode édition + retour liste */
    public void setModeEditReturnToList(Exercise ex, Runnable onDoneRefresh) {
        this.editing = ex;
        this.onDoneRefresh = onDoneRefresh;

        lblHeader.setText("Modifier l'exercice");
        btnSave.setText("Enregistrer");

        if (ex != null) {
            txtTitle.setText(nullToEmpty(ex.getTitle()));
            cbType.setValue(ex.getType());
            cbLevel.setValue(ex.getLevel());
            txtDuration.setText(String.valueOf(ex.getDurationMinutes()));
            txtDescription.setText(nullToEmpty(ex.getDescription()));
        }
    }

    // =============================
    // ACTIONS
    // =============================

    @FXML
    private void onSave() {
        try {
            Exercise ex = new Exercise();

            if (editing != null) {
                ex.setId(editing.getId());
            }

            ex.setTitle(txtTitle.getText());
            ex.setType(cbType.getValue());
            ex.setLevel(cbLevel.getValue() == null ? 1 : cbLevel.getValue());

            int dur = Integer.parseInt(txtDuration.getText().trim());
            ex.setDurationMinutes(dur);

            ex.setDescription(txtDescription.getText());

            if (editing == null) {
                exerciseService.addExercise(ex);
                showInfo("Ajout", "Exercice ajouté avec succès.");
            } else {
                exerciseService.updateExercise(ex);
                showInfo("Mise à jour", "Exercice modifié avec succès.");
            }

            if (onDoneRefresh != null) onDoneRefresh.run();
            goBackToList();

        } catch (NumberFormatException e) {
            showError("Erreur", "Durée invalide. Mets un nombre (minutes).");
        } catch (IllegalArgumentException e) {
            showError("Erreur validation", e.getMessage());
        } catch (SQLException e) {
            showError("Erreur BD", e.getMessage());
        }
    }


    @FXML
    private void onBack() {
        goBackToList();
    }

    // (kept in case something calls it)
    @FXML
    private void onCancel() {
        goBackToList();
    }



    private void goBackToList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/ExerciseList.fxml"));
            Parent root = loader.load();
            setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de retourner à la liste.");
        }
    }

    private void setContent(Parent page) {
        StackPane host = (StackPane) btnSave.getScene().lookup("#contentHost");
        if (host == null) {
            throw new IllegalStateException("contentHost introuvable. Vérifie fx:id=\"contentHost\" dans Template.fxml");
        }
        host.getChildren().setAll(page);
    }



    private void clearFields() {
        txtTitle.clear();
        cbType.getSelectionModel().clearSelection();
        cbLevel.setValue(1);
        txtDuration.clear();
        txtDescription.clear();
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

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
