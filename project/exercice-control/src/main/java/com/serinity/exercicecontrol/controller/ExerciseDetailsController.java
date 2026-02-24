package com.serinity.exercicecontrol.controller;

import com.serinity.exercicecontrol.dao.SessionDAO;
import com.serinity.exercicecontrol.model.Exercise;
import com.serinity.exercicecontrol.model.Resource;
import com.serinity.exercicecontrol.security.AuthContext;
import com.serinity.exercicecontrol.service.ExerciseService;
import com.serinity.exercicecontrol.service.ResourceService;
import com.serinity.exercicecontrol.service.SessionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ExerciseDetailsController {

    // -------- Exercise UI ----------
    @FXML private Label lblTitle;
    @FXML private Label lblType;
    @FXML private Label lblLevel;
    @FXML private Label lblDuration;
    @FXML private TextArea txtDescription;

    // -------- Resources UI ----------
    @FXML private Label lblResourcesInfo;
    @FXML private TilePane resourcesPane;

    private final ResourceService resourceService = new ResourceService();
    private final ExerciseService exerciseService = new ExerciseService();

    private Exercise exercise;

    public void setExercise(Exercise ex) {
        this.exercise = ex;
        fillExercise();
        refreshResources();
    }

    private void fillExercise() {
        if (exercise == null) return;

        lblTitle.setText(nvl(exercise.getTitle(), "(Sans titre)"));
        lblType.setText("Type : " + nvl(exercise.getType(), "-"));
        lblLevel.setText("Niveau : " + exercise.getLevel());
        lblDuration.setText("Durée : " + exercise.getDurationMinutes() + " min");
        txtDescription.setText(exercise.getDescription() == null ? "" : exercise.getDescription());
    }

    // ===================== ADMIN GUARD =====================

    private boolean requireAdmin() {
        if (AuthContext.isAdmin()) return true;
        showError("Accès refusé", "Action réservée à l'ADMIN.");
        return false;
    }

    // ===================== RESOURCES CRUD (UI) =====================

    @FXML
    private void onRefreshResources() {
        refreshResources();
    }

    private void refreshResources() {
        if (exercise == null) return;

        try {
            List<Resource> list = resourceService.getResourcesByExerciseId(exercise.getId());
            if (lblResourcesInfo != null) {
                lblResourcesInfo.setText("(" + list.size() + ")");
            }
            renderResources(list);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur BD", "Impossible de charger les ressources.\n" + e.getMessage());
        }
    }

    private void renderResources(List<Resource> list) {
        if (resourcesPane == null) return;

        resourcesPane.getChildren().clear();
        for (Resource r : list) {
            resourcesPane.getChildren().add(loadResourceCard(r));
        }
    }

    private Parent loadResourceCard(Resource r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/ResourceCard.fxml"));
            Parent root = loader.load();

            ResourceCardController ctrl = loader.getController();
            ctrl.setData(r, this);

            return root;
        } catch (IOException e) {
            throw new RuntimeException("Erreur chargement ResourceCard.fxml", e);
        }
    }

    @FXML
    private void onAddResource() {
        if (!requireAdmin()) return;
        if (exercise == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/ResourceForm.fxml"));
            Parent root = loader.load();

            ResourceFormController ctrl = loader.getController();
            ctrl.setModeCreateReturnToDetails(exercise, this::refreshResources);

            setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire Resource.");
        }
    }

    public void openEditResource(Resource r) {
        if (!requireAdmin()) return;
        if (exercise == null || r == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/ResourceForm.fxml"));
            Parent root = loader.load();

            ResourceFormController ctrl = loader.getController();
            ctrl.setModeEditReturnToDetails(exercise, r, this::refreshResources);

            setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la modification Resource.");
        }
    }

    public void deleteResource(Resource r) {
        if (!requireAdmin()) return;
        if (r == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer la ressource : " + nvl(r.getTitle(), "(Sans titre)") + " ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            resourceService.deleteResource(r.getId());
            refreshResources();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur suppression", e.getMessage());
        }
    }

    // ===================== EXERCISE ACTIONS =====================

    @FXML
    private void onEditExercise() {
        if (!requireAdmin()) return;
        if (exercise == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/ExerciseForm.fxml"));
            Parent root = loader.load();

            ExerciseFormController ctrl = loader.getController();
            ctrl.setModeEditReturnToList(exercise, null);

            setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la modification.");
        }
    }

    @FXML
    private void onDeleteExercise() {
        if (!requireAdmin()) return;
        if (exercise == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer l'exercice : " + nvl(exercise.getTitle(), "(Sans titre)") + " ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            exerciseService.deleteExercise(exercise.getId());
            onBack();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur BD", "Impossible de supprimer.\n" + e.getMessage());
        } catch (SecurityException se) {
            showError("Accès refusé", se.getMessage());
        }
    }

    // ✅ Start session (accessible à tous)
    @FXML
    private void onStart() {
        if (exercise == null) return;

        try {
            // ✅ Remplace par l'utilisateur connecté
            int userId = (AuthContext.userId() > 0) ? AuthContext.userId() : 1;

            SessionDAO dao = new SessionDAO();
            SessionService service = new SessionService(dao);

            int sessionId = dao.createCreatedSession(userId, exercise.getId());
            service.start(sessionId);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/SessionRun.fxml"));
            Parent root = loader.load();

            SessionRunController ctrl = loader.getController();
            ctrl.init(sessionId, exercise);

            setContent(root);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de démarrer la session.\n" + e.getMessage());
        }
    }

    @FXML
    private void onOpenHistory() {
        if (exercise == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/SessionHistory.fxml"));
            Parent root = loader.load();

            SessionHistoryController ctrl = loader.getController();
            ctrl.setExercise(exercise);

            setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir l'historique.");
        }
    }

    @FXML
    private void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/ExerciseList.fxml"));
            Parent root = loader.load();
            setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de retourner à la liste.");
        }
    }

    // ===================== TEMPLATE HOST =====================

    private void setContent(Parent page) {
        StackPane host = (StackPane) lblTitle.getScene().lookup("#contentHost");
        if (host == null) {
            throw new IllegalStateException("contentHost introuvable. Vérifie fx:id=\"contentHost\" dans Template.fxml");
        }
        host.getChildren().setAll(page);
    }

    private String nvl(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    @SuppressWarnings("unused")
    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}