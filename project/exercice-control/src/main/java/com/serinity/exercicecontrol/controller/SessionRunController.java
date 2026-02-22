package com.serinity.exercicecontrol.controller;

import com.serinity.exercicecontrol.dao.SessionDAO;
import com.serinity.exercicecontrol.model.Exercise;
import com.serinity.exercicecontrol.service.SessionService;
import com.serinity.exercicecontrol.service.SessionStatus;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;

public class SessionRunController {

    @FXML private Label lblTitle;
    @FXML private Label lblStatusPill;

    @FXML private Label lblTimer;
    @FXML private Label lblTarget;
    @FXML private Label lblGuidance;

    @FXML private TextArea txtFeedback;

    @FXML private Button btnStart;
    @FXML private Button btnPause;
    @FXML private Button btnResume;
    @FXML private Button btnComplete;
    @FXML private Button btnAbort;

    private final SessionDAO sessionDAO = new SessionDAO();
    private final SessionService sessionService = new SessionService(sessionDAO);

    private int sessionId = -1;
    private Exercise exercise;

    private Timeline uiTimer;

    // Appelé depuis ExerciseDetailsController après création + start
    public void init(int sessionId, Exercise exercise) {
        this.sessionId = sessionId;
        this.exercise = exercise;

        lblTitle.setText(exercise != null ? "Session — " + safe(exercise.getTitle(), "Exercice") : "Session");
        lblGuidance.setText(exercise != null ? safe(exercise.getDescription(), "—") : "—");
        lblTarget.setText(exercise != null ? (exercise.getDurationMinutes() + " min") : "—");

        refreshFromDb();
        startUiTimer();
    }

    @FXML
    private void onStart() {
        try {
            sessionService.start(sessionId);
            refreshFromDb();
        } catch (Exception e) {
            showError("Erreur", "Impossible de démarrer.\n" + e.getMessage());
        }
    }

    @FXML
    private void onPause() {
        try {
            sessionService.pause(sessionId);
            refreshFromDb();
        } catch (Exception e) {
            showError("Erreur", "Pause impossible.\n" + e.getMessage());
        }
    }

    @FXML
    private void onResume() {
        try {
            sessionService.resume(sessionId);
            refreshFromDb();
        } catch (Exception e) {
            showError("Erreur", "Reprise impossible.\n" + e.getMessage());
        }
    }

    @FXML
    private void onComplete() {
        try {
            String feedback = txtFeedback.getText();
            sessionService.complete(sessionId, feedback);
            refreshFromDb();
            stopUiTimer();
            showInfo("Terminé", "Session terminée ✅");
        } catch (Exception e) {
            showError("Erreur", "Impossible de terminer.\n" + e.getMessage());
        }
    }

    @FXML
    private void onAbort() {
        try {
            sessionService.abort(sessionId);
            refreshFromDb();
            stopUiTimer();
        } catch (Exception e) {
            showError("Erreur", "Impossible de quitter.\n" + e.getMessage());
        }
    }

    @FXML
    private void onBack() {
        // Retour aux détails de l'exercice
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/ExerciseDetails.fxml"));
            Parent root = loader.load();

            ExerciseDetailsController ctrl = loader.getController();
            ctrl.setExercise(exercise);

            setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de revenir aux détails.");
        }
    }

    // ---------------- UI refresh ----------------

    private void refreshFromDb() {
        try {
            // NOTE: on peut lire avec FOR UPDATE sans transaction long => OK,
            // mais mieux d’avoir une méthode findById (voir plus bas).
            var s = sessionDAO.findByIdForUpdate(sessionId);
            if (s == null) return;

            SessionStatus status = s.status();
            updatePill(status);

            // Timer affiché = active_seconds + (now-last_resumed_at) si IN_PROGRESS
            int active = s.activeSeconds();
            if (status == SessionStatus.IN_PROGRESS && s.lastResumedAt() != null) {
                active += Math.max(0, java.time.Duration.between(s.lastResumedAt(), LocalDateTime.now()).toSecondsPart()
                        + (int) java.time.Duration.between(s.lastResumedAt(), LocalDateTime.now()).toMinutes() * 60);
            }

            lblTimer.setText(formatMMSS(active));

            // Feedback
            if (s.feedback() != null && !s.feedback().isBlank() && (txtFeedback.getText() == null || txtFeedback.getText().isBlank())) {
                txtFeedback.setText(s.feedback());
            }

            updateButtons(status);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de rafraîchir la session.\n" + e.getMessage());
        }
    }

    private void updateButtons(SessionStatus status) {
        // Règle UX: on force reprise avant terminer
        switch (status) {
            case CREATED -> {
                btnStart.setDisable(false);
                btnPause.setDisable(true);
                btnResume.setDisable(true);
                btnComplete.setDisable(true);
                btnAbort.setDisable(true);
            }
            case IN_PROGRESS -> {
                btnStart.setDisable(true);
                btnPause.setDisable(false);
                btnResume.setDisable(true);
                btnComplete.setDisable(false);
                btnAbort.setDisable(false);
            }
            case PAUSED -> {
                btnStart.setDisable(true);
                btnPause.setDisable(true);
                btnResume.setDisable(false);
                btnComplete.setDisable(true);
                btnAbort.setDisable(false);
            }
            default -> { // COMPLETED / ABORTED / CANCELLED
                btnStart.setDisable(true);
                btnPause.setDisable(true);
                btnResume.setDisable(true);
                btnComplete.setDisable(true);
                btnAbort.setDisable(true);
            }
        }
    }

    private void updatePill(SessionStatus status) {
        // reset classes
        lblStatusPill.getStyleClass().removeAll("pill-success", "pill-info", "pill-warn", "pill-error");
        if (!lblStatusPill.getStyleClass().contains("pill")) lblStatusPill.getStyleClass().add("pill");

        switch (status) {
            case COMPLETED -> {
                lblStatusPill.setText("TERMINÉ");
                lblStatusPill.getStyleClass().add("pill-success");
            }
            case IN_PROGRESS -> {
                lblStatusPill.setText("EN COURS");
                lblStatusPill.getStyleClass().add("pill-info");
            }
            case PAUSED -> {
                lblStatusPill.setText("EN PAUSE");
                lblStatusPill.getStyleClass().add("pill-warn");
            }
            case ABORTED -> {
                lblStatusPill.setText("ARRÊTÉ");
                lblStatusPill.getStyleClass().add("pill-error");
            }
            case CREATED -> {
                lblStatusPill.setText("PRÊT");
                lblStatusPill.getStyleClass().add("pill-info");
            }
            default -> {
                lblStatusPill.setText(status.name());
                lblStatusPill.getStyleClass().add("pill-warn");
            }
        }
    }

    private void startUiTimer() {
        stopUiTimer();
        uiTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> refreshFromDb()));
        uiTimer.setCycleCount(Timeline.INDEFINITE);
        uiTimer.play();
    }

    private void stopUiTimer() {
        if (uiTimer != null) {
            uiTimer.stop();
            uiTimer = null;
        }
    }

    // ---------------- Template host ----------------

    private void setContent(Parent page) {
        StackPane host = (StackPane) lblTitle.getScene().lookup("#contentHost");
        if (host == null) {
            throw new IllegalStateException("contentHost introuvable. Vérifie fx:id=\"contentHost\" dans Template.fxml");
        }
        host.getChildren().setAll(page);
    }

    // ---------------- Helpers ----------------

    private String formatMMSS(int seconds) {
        if (seconds < 0) seconds = 0;
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    private String safe(String v, String fallback) {
        return (v == null || v.isBlank()) ? fallback : v;
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