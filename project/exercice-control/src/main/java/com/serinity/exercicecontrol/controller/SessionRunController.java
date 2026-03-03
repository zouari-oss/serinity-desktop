package com.serinity.exercicecontrol.controller;

import com.serinity.exercicecontrol.dao.SessionDAO;
import com.serinity.exercicecontrol.model.Exercise;
import com.serinity.exercicecontrol.model.api.VideoSuggestion;
import com.serinity.exercicecontrol.service.AmbientSoundApiService;
import com.serinity.exercicecontrol.service.SessionService;
import com.serinity.exercicecontrol.service.SessionStatus;
import com.serinity.exercicecontrol.service.WorldTimeApiService;
import com.serinity.exercicecontrol.service.api.YouTubeApiService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

    // API UI
    @FXML private Label lblDayPhase;
    @FXML private Label lblStation;
    @FXML private Button btnSoundLoad;
    @FXML private Button btnSoundPlay;
    @FXML private Button btnSoundStop;

    // YouTube UI
    @FXML private ListView<VideoSuggestion> listYouTube;
    @FXML private Label lblYouTubeStatus;

    private final SessionDAO sessionDAO = new SessionDAO();
    private final SessionService sessionService = new SessionService(sessionDAO);

    private final WorldTimeApiService timeApi = new WorldTimeApiService();
    private final AmbientSoundApiService soundApi = new AmbientSoundApiService();

    private YouTubeApiService youTubeApi; // lazy init

    private int sessionId = -1;
    private Exercise exercise;

    private Timeline uiTimer;

    // sound state
    private List<AmbientSoundApiService.Station> stations = Collections.emptyList();
    private int stationIndex = 0;
    private MediaPlayer ambientPlayer;

    // évite que des callbacks async update l’UI après fermeture
    private volatile boolean alive = true;

    public void init(int sessionId, Exercise exercise) {
        this.sessionId = sessionId;
        this.exercise = exercise;

        lblTitle.setText(exercise != null ? "Session — " + safe(exercise.getTitle(), "Exercice") : "Session");
        lblGuidance.setText(exercise != null ? safe(exercise.getDescription(), "—") : "—");
        lblTarget.setText(exercise != null ? (exercise.getDurationMinutes() + " min") : "—");

        initYouTubeUI();

        // ✅ Tout en async pour éviter freeze
        initDayPhaseAsync();
        loadAmbientAsync(true);     // charge et play auto
        onLoadYouTube();            // déjà async
        refreshFromDbAsync();       // 1er refresh
        startUiTimer();             // timer async
    }

    // ---------------- Day phase (ASYNC) ----------------

    private void initDayPhaseAsync() {
        if (lblDayPhase == null) return;

        CompletableFuture.supplyAsync(() -> {
            try {
                var info = timeApi.fetchTime("Africa/Tunis");
                return toFr(info.phase());
            } catch (Exception e) {
                int hour = LocalTime.now().getHour();
                return toFr(fromHour(hour));
            }
        }).thenAccept(phase -> Platform.runLater(() -> {
            if (!alive) return;
            lblDayPhase.setText(phase);
        }));
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

    // ---------------- Ambient (ASYNC) ----------------

    @FXML
    private void onLoadAmbient() {
        loadAmbientAsync(false);
    }

    private void loadAmbientAsync(boolean autoPlay) {
        if (lblStation == null || btnSoundPlay == null) return;

        btnSoundPlay.setDisable(true);
        lblStation.setText("Chargement ambiance...");

        final String phase = (lblDayPhase == null) ? "" : safe(lblDayPhase.getText(), "");

        CompletableFuture.supplyAsync(() -> {
            String query = switch (phase) {
                case "Nuit" -> "sleep";
                case "Soir" -> "meditation";
                default -> "relax";
            };
            try {
                return soundApi.searchStations(query, 25);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).whenComplete((result, err) -> Platform.runLater(() -> {
            if (!alive) return;

            if (err != null) {
                lblStation.setText("Erreur API");
                btnSoundPlay.setDisable(true);
                return;
            }

            stations = (result == null) ? List.of() : result;
            stationIndex = 0;

            if (stations.isEmpty()) {
                lblStation.setText("Aucun résultat doux");
                btnSoundPlay.setDisable(true);
                return;
            }

            lblStation.setText(stations.get(0).name());
            btnSoundPlay.setDisable(false);

            if (autoPlay) onPlayAmbient();
        }));
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

    // ---------------- YouTube (déjà ASYNC) ----------------

    private void initYouTubeUI() {
        if (listYouTube == null) return;

        listYouTube.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(VideoSuggestion item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    int min = item.durationSeconds() / 60;
                    int sec = item.durationSeconds() % 60;
                    setText(item.title() + " (" + min + "m " + sec + "s) — " + item.channelTitle());
                }
            }
        });

        listYouTube.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                VideoSuggestion v = listYouTube.getSelectionModel().getSelectedItem();
                if (v != null) openYouTube(v.videoId());
            }
        });
    }

    @FXML
    private void onLoadYouTube() {
        if (lblYouTubeStatus != null) lblYouTubeStatus.setText("Chargement YouTube...");

        if (youTubeApi == null) {
            try {
                youTubeApi = new YouTubeApiService();
            } catch (Exception e) {
                if (lblYouTubeStatus != null) {
                    lblYouTubeStatus.setText("Clé YouTube manquante: ajoute YOUTUBE_API_KEY dans .env");
                }
                if (listYouTube != null) listYouTube.setItems(FXCollections.observableArrayList());
                return;
            }
        }

        String query = buildYouTubeQuery();

        CompletableFuture.supplyAsync(() -> {
            try {
                var vids = youTubeApi.searchMeditationOrYoga(query, 10);
                int minSec = 3 * 60;
                int maxSec = 15 * 60;
                return youTubeApi.filterByDuration(vids, minSec, maxSec);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).whenComplete((videos, err) -> Platform.runLater(() -> {
            if (!alive) return;

            if (err != null) {
                if (lblYouTubeStatus != null) lblYouTubeStatus.setText("Erreur YouTube: " + rootMsg(err));
                return;
            }

            if (listYouTube != null) listYouTube.setItems(FXCollections.observableArrayList(videos));

            if (lblYouTubeStatus != null) {
                lblYouTubeStatus.setText(videos == null || videos.isEmpty()
                        ? "Aucune vidéo trouvée. Clique Rafraîchir."
                        : "Trouvé " + videos.size() + " vidéos. Double-clique pour ouvrir.");
            }
        }));
    }

    private String buildYouTubeQuery() {
        String base;
        if (exercise != null) {
            String t = safe(exercise.getTitle(), "").toLowerCase();
            if (t.contains("yoga")) base = "yoga for stress 10 minutes";
            else if (t.contains("medit")) base = "guided meditation anxiety 5 minutes";
            else base = "breathing meditation 5 minutes";
        } else {
            base = "guided meditation 5 minutes";
        }

        String phase = (lblDayPhase != null) ? safe(lblDayPhase.getText(), "") : "";
        if ("Nuit".equals(phase)) base = "sleep meditation 10 minutes";
        else if ("Soir".equals(phase)) base = "evening mindfulness meditation 10 minutes";

        return base;
    }

    private void openYouTube(String videoId) {
        try {
            String url = "https://www.youtube.com/watch?v=" + videoId;
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(URI.create(url));
            else showInfo("YouTube", "Ouvre manuellement: " + url);
        } catch (Exception e) {
            showError("YouTube", "Impossible d’ouvrir la vidéo.\n" + e.getMessage());
        }
    }

    // ---------------- Session actions (ASYNC) ----------------

    @FXML
    private void onStart() {
        runSessionActionAsync(() -> sessionService.start(sessionId), "Impossible de démarrer.");
    }

    @FXML
    private void onPause() {
        runSessionActionAsync(() -> sessionService.pause(sessionId), "Pause impossible.");
    }

    @FXML
    private void onResume() {
        runSessionActionAsync(() -> sessionService.resume(sessionId), "Reprise impossible.");
    }

    @FXML
    private void onComplete() {
        runSessionActionAsync(() -> sessionService.complete(sessionId, txtFeedback.getText()), "Impossible de terminer.");
        // stop local après update
        Platform.runLater(() -> {
            stopUiTimer();
            stopAmbient();
            showInfo("Terminé", "Session terminée ✅");
        });
    }

    @FXML
    private void onAbort() {
        runSessionActionAsync(() -> sessionService.abort(sessionId), "Impossible de quitter.");
        Platform.runLater(() -> {
            stopUiTimer();
            stopAmbient();
        });
    }

    private void runSessionActionAsync(Runnable action, String errMsg) {
        disableActionButtons(true);
        CompletableFuture.runAsync(() -> {
            try { action.run(); }
            catch (Exception e) { throw new RuntimeException(e); }
        }).whenComplete((ok, err) -> Platform.runLater(() -> {
            disableActionButtons(false);
            if (!alive) return;
            if (err != null) showError("Erreur", errMsg + "\n" + rootMsg(err));
            refreshFromDbAsync();
        }));
    }

    private void disableActionButtons(boolean v) {
        if (btnStart != null) btnStart.setDisable(v);
        if (btnPause != null) btnPause.setDisable(v);
        if (btnResume != null) btnResume.setDisable(v);
        if (btnComplete != null) btnComplete.setDisable(v);
        if (btnAbort != null) btnAbort.setDisable(v);
    }

    // ---------------- Back ----------------

    @FXML
    private void onBack() {
        alive = false;
        stopUiTimer();
        stopAmbient();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/ExerciseDetails.fxml"));
            Parent root = loader.load();
            ExerciseDetailsController ctrl = loader.getController();
            ctrl.setExercise(exercise);
            setContent(root);
        } catch (IOException e) {
            showError("Erreur", "Impossible de revenir aux détails.\n" + e.getMessage());
        }
    }

    // ---------------- DB refresh (ASYNC) ----------------

    private void refreshFromDbAsync() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return sessionDAO.findByIdForUpdate(sessionId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).whenComplete((s, err) -> Platform.runLater(() -> {
            if (!alive) return;

            if (err != null) {
                // on évite spam d'alert chaque seconde
                return;
            }
            if (s == null) return;

            SessionStatus status = s.status();
            updatePill(status);

            long active = s.activeSeconds();
            if (status == SessionStatus.IN_PROGRESS && s.lastResumedAt() != null) {
                long added = java.time.Duration.between(s.lastResumedAt(), LocalDateTime.now()).getSeconds();
                if (added > 0) active += added;
            }
            if (active < 0) active = 0;

            if (lblTimer != null) lblTimer.setText(formatMMSS((int) Math.min(Integer.MAX_VALUE, active)));

            if (s.feedback() != null && !s.feedback().isBlank()
                    && (txtFeedback.getText() == null || txtFeedback.getText().isBlank())) {
                txtFeedback.setText(s.feedback());
            }

            updateButtons(status);
        }));
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
        uiTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> refreshFromDbAsync()));
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

    private static String rootMsg(Throwable t) {
        Throwable x = t;
        while (x.getCause() != null) x = x.getCause();
        return x.getMessage() == null ? x.toString() : x.getMessage();
    }
}