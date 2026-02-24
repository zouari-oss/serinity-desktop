package com.serinity.exercicecontrol.controller;

import com.serinity.exercicecontrol.model.Exercise;
import com.serinity.exercicecontrol.service.ExerciseSessionService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;

public class ExerciseSessionController {

    @FXML private Label lblStatus;
    @FXML private Label lblTitle;
    @FXML private Label lblMeta;
    @FXML private Label lblTimer;

    @FXML private Button btnStart;
    @FXML private Button btnPause;
    @FXML private Button btnFinish;

    @FXML private TextArea txtFeedback;

    private final ExerciseSessionService sessionService = new ExerciseSessionService();


    private final int currentUserId = 1;

    private Exercise exercise;
    private int sessionId = -1;

    private Timeline timeline;
    private int remainingSeconds = 0;
    private boolean running = false;

    public void setExercise(Exercise ex) {
        this.exercise = ex;

        String title = (ex != null && ex.getTitle() != null) ? ex.getTitle() : "(Sans titre)";
        lblTitle.setText(title);

        String type = (ex != null && ex.getType() != null) ? ex.getType() : "-";
        int level = (ex != null) ? ex.getLevel() : 0;
        int minutes = (ex != null) ? ex.getDurationMinutes() : 0;

        lblMeta.setText("Type : " + type + " • Niveau : " + level + " • Durée : " + minutes + " min");

        remainingSeconds = Math.max(0, minutes * 60);
        lblTimer.setText(formatTime(remainingSeconds));
        lblStatus.setText("Prêt");
    }

    @FXML
    private void onStart() {
        if (exercise == null) return;

        // Create session in DB only once
        if (sessionId <= 0) {
            try {
                sessionId = sessionService.startSession(currentUserId, exercise.getId());
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur BD", "Impossible de démarrer la session.\n" + e.getMessage());
                return;
            }
        }

        if (timeline == null) {
            timeline = new Timeline(new KeyFrame(Duration.seconds(1), evt -> tick()));
            timeline.setCycleCount(Timeline.INDEFINITE);
        }

        running = true;
        timeline.play();

        lblStatus.setText("En cours");
        btnStart.setDisable(true);
        btnPause.setDisable(false);
        btnFinish.setDisable(false);
    }

    @FXML
    private void onPause() {
        if (timeline == null) return;

        if (running) {
            timeline.pause();
            running = false;
            lblStatus.setText("En pause");
            btnPause.setText("Reprendre");
        } else {
            timeline.play();
            running = true;
            lblStatus.setText("En cours");
            btnPause.setText("Pause");
        }
    }

    private void tick() {
        if (remainingSeconds > 0) {
            remainingSeconds--;
            lblTimer.setText(formatTime(remainingSeconds));
        } else {
            // auto-finish when time is up
            onFinish();
        }
    }

    @FXML
    private void onFinish() {
        if (timeline != null) {
            timeline.stop();
        }
        running = false;


        try {
            String feedback = (txtFeedback != null) ? txtFeedback.getText() : null;


            sessionService.completeSession(sessionId, feedback);

        } catch (Exception e) {

            e.printStackTrace();
        }

        lblStatus.setText("Terminée");
        btnStart.setDisable(true);
        btnPause.setDisable(true);
        btnFinish.setDisable(true);

        showInfo("Session terminée", "Bravo ✅ Session enregistrée.");
    }

    @FXML
    private void onBack() {

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

    private void setContent(Parent page) {
        StackPane host = (StackPane) lblTitle.getScene().lookup("#contentHost");
        if (host == null) {
            throw new IllegalStateException("contentHost introuvable. Vérifie fx:id=\"contentHost\" dans Template.fxml");
        }
        host.getChildren().setAll(page);
    }

    private String formatTime(int totalSeconds) {
        int m = totalSeconds / 60;
        int s = totalSeconds % 60;
        return String.format("%02d:%02d", m, s);
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
