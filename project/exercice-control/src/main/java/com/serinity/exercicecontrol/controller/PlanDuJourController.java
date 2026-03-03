package com.serinity.exercicecontrol.controller;

import com.serinity.exercicecontrol.model.api.VideoSuggestion;
import com.serinity.exercicecontrol.service.ContextAwarePlanner;
import com.serinity.exercicecontrol.service.FatigueEstimationService;
import com.serinity.exercicecontrol.service.ContextAwarePlanner.DailyContext;
import com.serinity.exercicecontrol.service.ContextAwarePlanner.DailyPlan;
import com.serinity.exercicecontrol.service.api.EnvConfig;
import com.serinity.exercicecontrol.service.api.WeatherApiService;
import com.serinity.exercicecontrol.service.api.YouTubeApiService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.net.URI;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlanDuJourController {

    @FXML private Label lblContext;
    @FXML private Label lblWeather;
    @FXML private Label lblWearable;      // affiche fatigue estimée (ex: 65/100)

    //  badges
    @FXML private Label lblFatigueBadge;
    @FXML private Label lblWeatherBadge;

    // slider fatigue
    @FXML private VBox boxFatigueFallback;
    @FXML private Slider sliderFatigue;
    @FXML private Label lblFatigueValue;

    // plan
    @FXML private Label lblActivity;
    @FXML private Label lblMeditation;
    @FXML private Label lblWhy;

    // YouTube
    @FXML private ListView<VideoSuggestion> listYouTube;
    @FXML private Label lblYouTubeStatus;

    private final WeatherApiService weatherApi = new WeatherApiService();
    private final ContextAwarePlanner planner = new ContextAwarePlanner();
    private final FatigueEstimationService fatigueEstimator = new FatigueEstimationService();

    private YouTubeApiService youTubeApi; // lazy

    @FXML
    private void initialize() {
        initYouTubeUI();
        initFatigueUI();
        refreshPlan();
    }

    private void initFatigueUI() {
        if (boxFatigueFallback != null) {
            boxFatigueFallback.setManaged(true);
            boxFatigueFallback.setVisible(true);
        }

        if (sliderFatigue != null && lblFatigueValue != null) {
            lblFatigueValue.setText((int) sliderFatigue.getValue() + "/100");
            sliderFatigue.valueProperty().addListener((obs, o, n) -> {
                lblFatigueValue.setText(n.intValue() + "/100");
            });
        }
    }

    private void initYouTubeUI() {
        if (listYouTube == null) return;

        listYouTube.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(VideoSuggestion item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
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
    private void onRefreshPlan() {
        refreshPlan();
    }

    private void refreshPlan() {
        if (lblYouTubeStatus != null) lblYouTubeStatus.setText("Calcul du plan…");
        if (listYouTube != null) listYouTube.setItems(FXCollections.observableArrayList());

        CompletableFuture.supplyAsync(() -> {
            try {
                // 1) Weather
                double lat = EnvConfig.getDouble("USER_LAT", 36.8065);
                double lon = EnvConfig.getDouble("USER_LON", 10.1815);
                WeatherApiService.WeatherNow w = weatherApi.fetchNow(lat, lon);

                // 2) fatigue: slider + estimation intelligente
                int userFatigue = (sliderFatigue != null) ? (int) sliderFatigue.getValue() : 50;
                int fatigueScore = fatigueEstimator.estimate(userFatigue, w);

                // 3) moment
                boolean eveningOrNight = isEveningOrNight();

                // 4) Planner (fusion météo + fatigue + moment + temp/vent)
                DailyContext ctx = new DailyContext(
                        w.isRaining(),
                        eveningOrNight,
                        fatigueScore,
                        w.temperatureC(),
                        w.windKph()
                );

                DailyPlan plan = planner.build(ctx);

                // 5) YouTube
                List<VideoSuggestion> vids = List.of();
                try {
                    if (youTubeApi == null) youTubeApi = new YouTubeApiService();
                    vids = youTubeApi.searchMeditationOrYoga(plan.youtubeQuery(), 12);
                    vids = youTubeApi.filterByDuration(vids, 3 * 60, 20 * 60);
                } catch (Exception ignored) {}

                return new Result(w, fatigueScore, plan, vids);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(res -> Platform.runLater(() -> {
            // météo
            if (lblWeather != null) {
                lblWeather.setText(String.format("%.0f°C • pluie: %.1fmm • vent: %.0f km/h",
                        res.weather.temperatureC(),
                        res.weather.precipitationMm(),
                        res.weather.windKph()));
            }

            // fatigue estimée
            if (lblWearable != null) {
                lblWearable.setText(res.fatigueScore + "/100");
            }

            //  badges
            applyBadges(res.fatigueScore, res.weather);

            // contexte
            if (lblContext != null) {
                lblContext.setText(res.plan.contextLine());
            }

            // plan
            if (lblActivity != null) {
                lblActivity.setText(res.plan.activityTitle() + " — " + res.plan.activityMinutes() + " min");
            }
            if (lblMeditation != null) {
                lblMeditation.setText(res.plan.meditationTitle() + " — " + res.plan.meditationMinutes() + " min");
            }
            if (lblWhy != null) {
                lblWhy.setText(res.plan.why());
            }

            // videos
            if (listYouTube != null) {
                listYouTube.setItems(FXCollections.observableArrayList(res.videos));
            }
            if (lblYouTubeStatus != null) {
                lblYouTubeStatus.setText(res.videos.isEmpty()
                        ? "Aucune vidéo (ou clé YouTube manquante)."
                        : "Trouvé " + res.videos.size() + " vidéos. Double-clique pour ouvrir.");
            }

        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                if (lblYouTubeStatus != null) lblYouTubeStatus.setText("Erreur: " + ex.getMessage());
            });
            return null;
        });
    }

    private boolean isEveningOrNight() {
        int h = LocalTime.now().getHour();
        return (h >= 18 || h < 6);
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

    @FXML
    private void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/ExerciseList.fxml"));
            Parent page = loader.load();
            setContent(page);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de revenir.\n" + e.getMessage());
        }
    }

    private void setContent(Parent page) {
        StackPane host = (StackPane) lblContext.getScene().lookup("#contentHost");
        if (host == null) throw new IllegalStateException("contentHost introuvable. Vérifie Template.fxml");
        host.getChildren().setAll(page);
    }

    private void applyBadges(int fatigueScore, WeatherApiService.WeatherNow w) {
        // fatigue badge
        if (lblFatigueBadge != null) {
            lblFatigueBadge.getStyleClass().removeAll("badge-green", "badge-orange", "badge-red");
            if (!lblFatigueBadge.getStyleClass().contains("badge")) lblFatigueBadge.getStyleClass().add("badge");

            if (fatigueScore >= 70) {
                lblFatigueBadge.setText("Fatigue élevée");
                lblFatigueBadge.getStyleClass().add("badge-red");
            } else if (fatigueScore >= 40) {
                lblFatigueBadge.setText("Fatigue modérée");
                lblFatigueBadge.getStyleClass().add("badge-orange");
            } else {
                lblFatigueBadge.setText("Bonne énergie");
                lblFatigueBadge.getStyleClass().add("badge-green");
            }
        }

        // météo badge (indoor/outdoor)
        if (lblWeatherBadge != null) {
            lblWeatherBadge.getStyleClass().removeAll("badge-green", "badge-orange", "badge-red");
            if (!lblWeatherBadge.getStyleClass().contains("badge")) lblWeatherBadge.getStyleClass().add("badge");

            boolean extremeTemp = (w.temperatureC() <= 8) || (w.temperatureC() >= 32);
            boolean strongWind = w.windKph() >= 30;

            if (w.isRaining() || extremeTemp || strongWind) {
                lblWeatherBadge.setText("Indoor conseillé");
                lblWeatherBadge.getStyleClass().add("badge-orange");
            } else {
                lblWeatherBadge.setText("Outdoor OK");
                lblWeatherBadge.getStyleClass().add("badge-green");
            }
        }
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

    private record Result(WeatherApiService.WeatherNow weather, int fatigueScore, DailyPlan plan, List<VideoSuggestion> videos) {}
}