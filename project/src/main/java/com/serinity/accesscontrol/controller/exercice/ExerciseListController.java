package com.serinity.accesscontrol.controller.exercice;

import com.serinity.accesscontrol.controller.exercice.admin.AdminDashboardController;
import com.serinity.accesscontrol.dao.exercice.RecommendationDAO;
import com.serinity.accesscontrol.dao.exercice.ScoringDAO;
import com.serinity.accesscontrol.model.exercice.Exercise;
import com.serinity.accesscontrol.service.exercice.ExerciseService;
import com.serinity.accesscontrol.service.exercice.RecommendationService;
import com.serinity.accesscontrol.service.exercice.ScoringService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class ExerciseListController {

    @FXML private Label lblCount;
    @FXML private ComboBox<String> cbType;
    @FXML private ComboBox<Integer> cbLevel;

    @FXML private TilePane cardsPane;

    @FXML private StackPane ambianceBox;
    @FXML private ImageView imgAmbiance;
    @FXML private Label lblAmbianceText;

    @FXML private Label lblScore;
    @FXML private Label lblStreak;
    @FXML private Label lblCompletion;
    @FXML private Label lblActive7d;
    @FXML private Label lblScorePill;

    @FXML private Label lblRecTitle;
    @FXML private Label lblRecMeta;
    @FXML private Label lblRecReason;
    @FXML private Button btnStartRecommended;

    @FXML private Button btnBreathing;

    private final ExerciseService exerciseService = new ExerciseService();
    private final ScoringService scoringService = new ScoringService(new ScoringDAO());
    private final RecommendationService recommendationService = new RecommendationService(new RecommendationDAO());

    private List<Exercise> allExercises = new ArrayList<>();
    private Exercise recommendedExercise = null;
    private int lastScore100 = 0;

    @FXML
    public void initialize() {
        cbType.setItems(FXCollections.observableArrayList("Tous"));
        cbType.getSelectionModel().selectFirst();

        cbLevel.setItems(FXCollections.observableArrayList());
        cbLevel.getItems().add(null);
        cbLevel.getItems().addAll(1, 2, 3, 4, 5);

        cbLevel.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Integer v) { return v == null ? "Tous" : String.valueOf(v); }
            @Override public Integer fromString(String s) {
                return (s == null || s.equalsIgnoreCase("Tous")) ? null : Integer.valueOf(s);
            }
        });
        cbLevel.getSelectionModel().selectFirst();

        if (ambianceBox != null) {
            ambianceBox.widthProperty().addListener((o, a, b) -> updateCoverViewport());
            ambianceBox.heightProperty().addListener((o, a, b) -> updateCoverViewport());
        }


        Platform.runLater(() -> {
            if (cardsPane == null) return;

            Scene scene = cardsPane.getScene();
            if (scene != null) {
                scene.widthProperty().addListener((o, a, b) -> updateColumns());
                updateColumns();
            } else {
                cardsPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) {
                        newScene.widthProperty().addListener((o, a, b) -> updateColumns());
                        updateColumns();
                    }
                });
            }
        });

        refresh();
        loadScoreAndRecommendation();
        loadAmbianceBanner();
    }

    private void updateColumns() {
        if (cardsPane == null || cardsPane.getScene() == null) return;

        double w = cardsPane.getScene().getWidth();
        double tile = cardsPane.getPrefTileWidth();
        double gap = cardsPane.getHgap();

        int cols = (int) Math.max(1, Math.floor((w - 80) / (tile + gap)));
        cols = Math.min(cols, 3);
        cardsPane.setPrefColumns(cols);
    }

    // ===================== Actions =====================

    @FXML
    private void onRefresh() {
        refresh();
        loadScoreAndRecommendation();
        loadAmbianceBanner();
    }

    @FXML
    private void onApplyFilters() {
        applyFiltersAndRender();
    }

    @FXML
    private void onClearFilters() {
        cbType.getSelectionModel().selectFirst();
        cbLevel.getSelectionModel().selectFirst();
        applyFiltersAndRender();
    }

    @FXML
    private void onAddExercise() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/exercice/ExerciseForm.fxml"));
            Parent root = loader.load();

            ExerciseFormController ctrl = loader.getController();
            ctrl.setModeCreateReturnToList(() -> {
                refresh();
                loadScoreAndRecommendation();
                loadAmbianceBanner();
            });

            setContentSafe(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire d'ajout.");
        }
    }

    @FXML
    private void onStartRecommended() {
        if (recommendedExercise == null) return;
        openDetails(recommendedExercise);
    }

    @FXML
    private void onOpenBreathing() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/breathing/BreathingView.fxml"));
            Parent root = loader.load();
            setContentSafe(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le module respiration.");
        }
    }

    // ===================== COACH =====================

    @FXML
    private void onOpenCoachDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/coach/CoachDashboard.fxml"));
            Parent root = loader.load();

            CoachDashboardController ctrl = loader.getController();

            StackPane host = getContentHost();
            int userId = 1; // TODO user connecté

            ctrl.setHost(host);
            ctrl.setContext(userId, allExercises, () -> {
                try {
                    FXMLLoader back = new FXMLLoader(getClass().getResource("/fxml/exercice/exercice/ExerciseList.fxml"));
                    Parent backRoot = back.load();
                    host.getChildren().setAll(backRoot);
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Erreur", "Impossible de revenir à la liste.\n" + e.getMessage());
                }
            });

            host.getChildren().setAll(root);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le Coach IA.\n" + e.getMessage());
        }
    }

    // ===================== ADMIN  =====================
    @FXML
    private void onOpenAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/admin/AdminDashboard.fxml"));
            Parent root = loader.load();

            StackPane host = getContentHost();

            AdminDashboardController ctrl = loader.getController();


            ctrl.setOnBack(() -> {
                try {
                    FXMLLoader back = new FXMLLoader(getClass().getResource("/fxml/exercice/exercice/ExerciseList.fxml"));
                    Parent backRoot = back.load();
                    host.getChildren().setAll(backRoot);
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Erreur", "Impossible de revenir à la liste.\n" + e.getMessage());
                }
            });

            host.getChildren().setAll(root);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir l'Admin Dashboard.\n" + e.getMessage());
        }
    }



    public void openEdit(Exercise ex) {
        if (ex == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/exercice/ExerciseForm.fxml"));
            Parent root = loader.load();

            ExerciseFormController ctrl = loader.getController();
            ctrl.setModeEditReturnToList(ex, () -> {
                refresh();
                loadScoreAndRecommendation();
                loadAmbianceBanner();
            });

            setContentSafe(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la modification.");
        }
    }

    public void deleteExercise(Exercise ex) {
        if (ex == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer l'exercice : " + safe(ex.getTitle(), "(Sans titre)") + " ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            exerciseService.deleteExercise(ex.getId());
            refresh();
            loadScoreAndRecommendation();
            loadAmbianceBanner();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de supprimer.\n" + e.getMessage());
        }
    }

    public void openDetails(Exercise ex) {
        if (ex == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/exercice/ExerciseDetails.fxml"));
            Parent root = loader.load();

            ExerciseDetailsController ctrl = loader.getController();
            ctrl.setExercise(ex);

            setContentSafe(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir les détails.");
        }
    }



    public void refresh() {
        try {
            allExercises = exerciseService.getAllExercises();

            Set<String> types = allExercises.stream()
                    .map(Exercise::getType)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toCollection(TreeSet::new));

            String selectedType = cbType.getValue();
            cbType.getItems().setAll("Tous");
            cbType.getItems().addAll(types);

            if (selectedType != null && cbType.getItems().contains(selectedType)) cbType.setValue(selectedType);
            else cbType.getSelectionModel().selectFirst();

            applyFiltersAndRender();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur BD", "Impossible de charger les exercices.\n" + e.getMessage());
        }
    }

    private void applyFiltersAndRender() {
        String type = cbType.getValue();
        Integer level = cbLevel.getValue();

        List<Exercise> filtered = allExercises.stream()
                .filter(ex -> {
                    boolean okType = (type == null || type.equalsIgnoreCase("Tous"))
                            || (ex.getType() != null && ex.getType().equalsIgnoreCase(type));
                    boolean okLevel = (level == null) || ex.getLevel() == level;
                    return okType && okLevel;
                })
                .toList();

        lblCount.setText("(" + filtered.size() + ")");
        renderCards(filtered);
    }

    private void renderCards(List<Exercise> list) {
        cardsPane.getChildren().clear();
        for (Exercise ex : list) {
            cardsPane.getChildren().add(loadExerciseCard(ex));
        }
    }

    private Parent loadExerciseCard(Exercise ex) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/exercice/ExerciseCard.fxml"));
            Parent root = loader.load();

            ExerciseCardController ctrl = loader.getController();
            ctrl.setData(ex, this);

            return root;
        } catch (IOException e) {
            throw new RuntimeException("Erreur chargement ExerciseCard.fxml", e);
        }
    }

    // ===================== Ambiance Banner =====================

    private void loadAmbianceBanner() {
        if (imgAmbiance == null || lblAmbianceText == null) return;

        int h = LocalTime.now().getHour();

        String imagePath;
        String text;

        if (h >= 6 && h < 12) {
            imagePath = "/images/morning.jpg";
            text = "🌄 Matin – Moment parfait pour démarrer en douceur.";
        } else if (h >= 12 && h < 18) {
            imagePath = "/images/afternoon.jpg";
            text = "🌿 Après-midi – Rechargez votre énergie.";
        } else if (h >= 18 && h < 22) {
            imagePath = "/images/evening.jpg";
            text = "🌇 Soir – Relaxation et recentrage.";
        } else {
            imagePath = "/images/night.jpg";
            text = "🌙 Nuit – Détente profonde.";
        }

        lblAmbianceText.setText(text);

        try {
            Image img = new Image(Objects.requireNonNull(getClass().getResource(imagePath)).toExternalForm());
            imgAmbiance.setImage(img);

            imgAmbiance.fitWidthProperty().bind(ambianceBox.widthProperty());
            imgAmbiance.fitHeightProperty().bind(ambianceBox.heightProperty());

            Platform.runLater(this::updateCoverViewport);

        } catch (Exception e) {
            imgAmbiance.setImage(null);
            lblAmbianceText.setText(text + " (image manquante)");
        }
    }

    private void updateCoverViewport() {
        if (imgAmbiance == null || ambianceBox == null) return;
        Image img = imgAmbiance.getImage();
        if (img == null) return;

        double boxW = ambianceBox.getWidth();
        double boxH = ambianceBox.getHeight();
        if (boxW <= 0 || boxH <= 0) return;

        double imgW = img.getWidth();
        double imgH = img.getHeight();
        if (imgW <= 0 || imgH <= 0) return;

        double boxRatio = boxW / boxH;
        double imgRatio = imgW / imgH;

        double viewW, viewH;

        if (imgRatio > boxRatio) {
            viewH = imgH;
            viewW = imgH * boxRatio;
        } else {
            viewW = imgW;
            viewH = imgW / boxRatio;
        }

        double x = (imgW - viewW) / 2.0;
        double y = (imgH - viewH) / 2.0;

        imgAmbiance.setViewport(new Rectangle2D(x, y, viewW, viewH));
    }

    // ===================== Score + Recommendation =====================

    private void loadScoreAndRecommendation() {
        int userId = 1;

        try {
            ScoringService.ScoreResult score = scoringService.computeEngagementScore(userId);
            lastScore100 = score.score100();

            lblScore.setText(score.score100() + "/100 (" + score.levelText() + ")");
            lblStreak.setText(score.streakDays() + " j");
            lblCompletion.setText(score.completionRatePercent() + "%");
            lblActive7d.setText(score.activeTime7dText());

            lblScorePill.getStyleClass().removeAll("pill-success", "pill-warn", "pill-info");
            if (!lblScorePill.getStyleClass().contains("pill")) lblScorePill.getStyleClass().add("pill");

            if (score.score100() >= 70) {
                lblScorePill.setText("EXCELLENT");
                lblScorePill.getStyleClass().add("pill-success");
            } else if (score.score100() >= 40) {
                lblScorePill.setText("BON");
                lblScorePill.getStyleClass().add("pill-info");
            } else {
                lblScorePill.setText("À RENFORCER");
                lblScorePill.getStyleClass().add("pill-warn");
            }
        } catch (Exception ignored) {}

        try {
            RecommendationService.RecommendationResult rec =
                    recommendationService.recommend(userId, lastScore100, allExercises);

            if (rec == null || rec.exercise() == null || rec.actionPlan() == null) {
                recommendedExercise = null;
                lblRecTitle.setText("—");
                lblRecMeta.setText("—");
                lblRecReason.setText("—");
                btnStartRecommended.setDisable(true);
                if (btnBreathing != null) btnBreathing.setDisable(false);
                return;
            }

            recommendedExercise = rec.exercise();

            lblRecTitle.setText(safe(recommendedExercise.getTitle(), "Exercice recommandé"));

            lblRecMeta.setText(
                    "Plan : " + rec.actionPlan().getPlannedMinutes() + " min à " + rec.actionPlan().plannedTimeLabel()
                            + " • Niveau : " + recommendedExercise.getLevel()
            );

            lblRecReason.setText(
                    safe(rec.reason(), "Recommandation basée sur votre activité récente.")
                            + "\n" + rec.actionPlan().getStreakMessage()
                            + "\n" + rec.actionPlan().getMicroCommitment()
            );

            btnStartRecommended.setDisable(false);
            if (btnBreathing != null) btnBreathing.setDisable(false);

        } catch (Exception e) {
            recommendedExercise = null;
            lblRecTitle.setText("—");
            lblRecMeta.setText("—");
            lblRecReason.setText("Impossible de générer une recommandation.");
            btnStartRecommended.setDisable(true);
            if (btnBreathing != null) btnBreathing.setDisable(false);
        }
    }

    @FXML
    private void onOpenPlan() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/exercice/PlanDuJour.fxml"));
            Parent page = loader.load();
            setContentSafe(page);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir Plan du jour.\n" + e.getMessage());
        }
    }

    private StackPane getContentHost() {
        if (cardsPane == null || cardsPane.getScene() == null) {
            throw new IllegalStateException("Scene non prête (cardsPane/scene null).");
        }
        StackPane host = (StackPane) cardsPane.getScene().lookup("#contentHost");
        if (host == null) host = (StackPane) cardsPane.getScene().lookup("#contentHostStackPane");
        if (host == null) throw new IllegalStateException("contentHost introuvable dans Template.fxml");
        return host;
    }

    private void setContentSafe(Parent page) {
        if (cardsPane == null || cardsPane.getScene() == null) {
            Platform.runLater(() -> setContentSafe(page));
            return;
        }
        StackPane host = (StackPane) cardsPane.getScene().lookup("#contentHost");
        if (host == null) host = (StackPane) cardsPane.getScene().lookup("#contentHostStackPane");
        if (host == null) throw new IllegalStateException("contentHost introuvable dans Template.fxml");
        host.getChildren().setAll(page);
    }

    private static String safe(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
