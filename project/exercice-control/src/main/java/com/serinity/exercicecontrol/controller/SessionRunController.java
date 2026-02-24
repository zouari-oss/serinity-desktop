package com.serinity.exercicecontrol.controller;

import com.serinity.exercicecontrol.dao.SessionDAO;
import com.serinity.exercicecontrol.model.Exercise;
import com.serinity.exercicecontrol.service.AmbientSoundApiService;
import com.serinity.exercicecontrol.service.SessionService;
import com.serinity.exercicecontrol.service.SessionStatus;
import com.serinity.exercicecontrol.service.WorldTimeApiService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

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

    // ✅ API UI (doit matcher SessionRun.fxml)
    @FXML private Label lblDayPhase;
    @FXML private Label lblStation;
    @FXML private Button btnSoundLoad;
    @FXML private Button btnSoundPlay;
    @FXML private Button btnSoundStop;

    private final SessionDAO sessionDAO = new SessionDAO();
    private final SessionService sessionService = new SessionService(sessionDAO);

    private final WorldTimeApiService timeApi = new WorldTimeApiService();
    private final AmbientSoundApiService soundApi = new AmbientSoundApiService();

    private int sessionId = -1;
    private Exercise exercise;

    private Timeline uiTimer;

    // sound state
    private List<AmbientSoundApiService.Station> stations = Collections.emptyList();
    private int stationIndex = 0;
    private MediaPlayer ambientPlayer;

    // Appelé depuis ExerciseDetailsController
    public void init(int sessionId, Exercise exercise) {
        this.sessionId = sessionId;
        this.exercise = exercise;

        lblTitle.setText(exercise != null ? "Session — " + safe(exercise.getTitle(), "Exercice") : "Session");
        lblGuidance.setText(exercise != null ? safe(exercise.getDescription(), "—") : "—");
        lblTarget.setText(exercise != null ? (exercise.getDurationMinutes() + " min") : "—");

        initDayPhase();

        // ✅ AUTO: charge automatiquement une station adaptée
        onLoadAmbient();

        // ✅ AUTO (optionnel mais demandé): démarre automatiquement la musique
        // Si tu veux seulement charger sans jouer, commente la ligne suivante.
        onPlayAmbient();

        refreshFromDb();
        startUiTimer();
    }

    // ---------------- API: day phase ----------------

    private void initDayPhase() {
        if (lblDayPhase == null) return;

        try {
            var info = timeApi.fetchTime("Africa/Tunis");
            lblDayPhase.setText(toFr(info.phase()));
        } catch (Exception e) {
            int hour = LocalTime.now().getHour();
            lblDayPhase.setText(toFr(fromHour(hour)));
        }
    }

    private WorldTimeApiService.DayPhase fromHour(int hour) {
        if (hour >= 6 && hour < 12) return WorldTimeApiService.DayPhase.MORNING;
        if (hour >= 12 && hour < 18) return WorldTimeApiService.DayPhase.AFTERNOON;
        if (hour >= 18 && hour < 22) return WorldTimeApiService.DayPhase.EVENING;
        return WorldTimeApiService.DayPhase.NIGHT;
    }

    private String toFr(WorldTimeApiService.DayPhase phase) {
        return switch (phase) {
            case MORNING -> "Matin";
            case AFTERNOON -> "Après-midi";
            case EVENING -> "Soir";
            case NIGHT -> "Nuit";
        };
    }

    // ---------------- API: ambient sounds ----------------

    @FXML
    private void onLoadAmbient() {
        if (lblStation == null || btnSoundPlay == null) return;

        try {
            String phase = lblDayPhase == null ? "" : safe(lblDayPhase.getText(), "");

            String query = switch (phase) {
                case "Nuit" -> "sleep";
                case "Soir" -> "meditation";
                default -> "relax";
            };

            stations = soundApi.searchStations(query, 25);
            stationIndex = 0;

            if (stations.isEmpty()) {
                lblStation.setText("Aucun résultat doux");
                btnSoundPlay.setDisable(true);
                return;
            }

            lblStation.setText(stations.get(0).name());
            btnSoundPlay.setDisable(false);

        } catch (Exception e) {
            lblStation.setText("Erreur API");
            btnSoundPlay.setDisable(true);
        }
    }

    @FXML
    private void onPlayAmbient() {
        if (stations == null || stations.isEmpty()) return;

        try {
            stopAmbient();

            AmbientSoundApiService.Station st = stations.get(stationIndex);
            if (lblStation != null) lblStation.setText(st.name());

            ambientPlayer = new MediaPlayer(new Media(st.streamUrl()));
            ambientPlayer.play();

        } catch (Exception e) {
            if (lblStation != null) lblStation.setText("Stream non lisible");
            stopAmbient();
        }
    }

    @FXML
    private void onStopAmbient() {
        stopAmbient();
    }

    private void stopAmbient() {
        if (ambientPlayer != null) {
            try { ambientPlayer.stop(); } catch (Exception ignored) {}
            try { ambientPlayer.dispose(); } catch (Exception ignored) {}
            ambientPlayer = null;
        }
    }

    // ---------------- Session actions ----------------

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
            sessionService.complete(sessionId, txtFeedback.getText());
            refreshFromDb();
            stopUiTimer();
            stopAmbient();
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
            stopAmbient();
        } catch (Exception e) {
            showError("Erreur", "Impossible de quitter.\n" + e.getMessage());
        }
    }

    @FXML
    private void onBack() {
        stopAmbient();

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
            var s = sessionDAO.findByIdForUpdate(sessionId);
            if (s == null) return;

            SessionStatus status = s.status();
            updatePill(status);

            long active = s.activeSeconds();
            if (status == SessionStatus.IN_PROGRESS && s.lastResumedAt() != null) {
                long added = java.time.Duration.between(s.lastResumedAt(), LocalDateTime.now()).getSeconds();
                if (added > 0) active += added;
            }
            if (active < 0) active = 0;

            lblTimer.setText(formatMMSS((int) Math.min(Integer.MAX_VALUE, active)));

            if (s.feedback() != null && !s.feedback().isBlank()
                    && (txtFeedback.getText() == null || txtFeedback.getText().isBlank())) {
                txtFeedback.setText(s.feedback());
            }

            updateButtons(status);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de rafraîchir la session.\n" + e.getMessage());
        }
    }

    private void updateButtons(SessionStatus status) {
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
            default -> {
                btnStart.setDisable(true);
                btnPause.setDisable(true);
                btnResume.setDisable(true);
                btnComplete.setDisable(true);
                btnAbort.setDisable(true);
            }
        }
    }

    private void updatePill(SessionStatus status) {
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