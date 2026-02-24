package com.serinity.exercicecontrol.controller;

import com.serinity.exercicecontrol.dao.RecommendationDAO;
import com.serinity.exercicecontrol.dao.ScoringDAO;
import com.serinity.exercicecontrol.model.Exercise;
import com.serinity.exercicecontrol.service.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;

import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class ExerciseListController {

    @FXML private Label lblCount;
    @FXML private ComboBox<String> cbType;
    @FXML private ComboBox<Integer> cbLevel;
    @FXML private FlowPane cardsPane;

    // ✅ Ambiance Banner
    @FXML private StackPane ambianceBox;
    @FXML private ImageView imgAmbiance;
    @FXML private Label lblAmbianceText;

    // ✅ Scoring UI
    @FXML private Label lblScore;
    @FXML private Label lblStreak;
    @FXML private Label lblCompletion;
    @FXML private Label lblActive7d;
    @FXML private Label lblScorePill;

    // ✅ Recommendation UI
    @FXML private Label lblRecTitle;
    @FXML private Label lblRecMeta;
    @FXML private Label lblRecReason;
    @FXML private Button btnStartRecommended;

    private final ExerciseService exerciseService = new ExerciseService();
    private final ScoringService scoringService = new ScoringService(new ScoringDAO());
    private final RecommendationService recommendationService = new RecommendationService(new RecommendationDAO());

    private List<Exercise> allExercises = new ArrayList<>();
    private Exercise recommendedExercise = null;
    private int lastScore100 = 0;

    @FXML
    public void initialize() {

        // combos
        cbType.setItems(FXCollections.observableArrayList("Tous"));
        cbType.getSelectionModel().selectFirst();

        cbLevel.setItems(FXCollections.observableArrayList());
        cbLevel.getItems().add(null);
        cbLevel.getItems().addAll(1, 2, 3, 4, 5);
        cbLevel.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Integer v) { return v == null ? "Tous" : String.valueOf(v); }
            @Override public Integer fromString(String s) { return (s == null || s.equalsIgnoreCase("Tous")) ? null : Integer.valueOf(s); }
        });
        cbLevel.getSelectionModel().selectFirst();

        // ✅ préparer "cover" quand la box resize
        if (ambianceBox != null) {
            ambianceBox.widthProperty().addListener((o, a, b) -> updateCoverViewport());
            ambianceBox.heightProperty().addListener((o, a, b) -> updateCoverViewport());
        }

        refresh();
        loadScoreAndRecommendation();
        loadAmbianceBanner();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/ExerciseForm.fxml"));
            Parent root = loader.load();

            ExerciseFormController ctrl = loader.getController();
            ctrl.setModeCreateReturnToList(() -> {
                refresh();
                loadScoreAndRecommendation();
                loadAmbianceBanner();
            });

            setContent(root);
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

    // ===================== API utilisée par ExerciseCardController =====================

    public void openEdit(Exercise ex) {
        if (ex == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/ExerciseForm.fxml"));
            Parent root = loader.load();

            ExerciseFormController ctrl = loader.getController();
            ctrl.setModeEditReturnToList(ex, () -> {
                refresh();
                loadScoreAndRecommendation();
                loadAmbianceBanner();
            });

            setContent(root);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/ExerciseDetails.fxml"));
            Parent root = loader.load();

            ExerciseDetailsController ctrl = loader.getController();
            ctrl.setExercise(ex);

            setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir les détails.");
        }
    }

    // ===================== Data =====================

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/ExerciseCard.fxml"));
            Parent root = loader.load();

            ExerciseCardController ctrl = loader.getController();
            ctrl.setData(ex, this);

            return root;
        } catch (IOException e) {
            throw new RuntimeException("Erreur chargement ExerciseCard.fxml", e);
        }
    }

    // ===================== Ambiance Banner (LOCAL IMAGES) =====================

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

            // bind taille + faire "cover"
            imgAmbiance.fitWidthProperty().bind(ambianceBox.widthProperty());
            imgAmbiance.fitHeightProperty().bind(ambianceBox.heightProperty());

            Platform.runLater(this::updateCoverViewport);

        } catch (Exception e) {
            imgAmbiance.setImage(null);
            lblAmbianceText.setText(text + " (image manquante)");
        }
    }

    /**
     * Fait un rendu type "background-size: cover" :
     * l'image remplit toute la box (crop au besoin) sans déformation.
     */
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
            // image plus large -> crop côtés
            viewH = imgH;
            viewW = imgH * boxRatio;
        } else {
            // image plus haute -> crop haut/bas
            viewW = imgW;
            viewH = imgW / boxRatio;
        }

        double x = (imgW - viewW) / 2.0;
        double y = (imgH - viewH) / 2.0;

        imgAmbiance.setViewport(new Rectangle2D(x, y, viewW, viewH));
    }

    // ===================== Score + Recommendation =====================

    private void loadScoreAndRecommendation() {
        int userId = 1; // TODO user connecté

        // SCORE
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

        // RECOMMENDATION
        try {
            RecommendationService.RecommendationResult rec =
                    recommendationService.recommend(userId, lastScore100, allExercises);

            if (rec == null || rec.exercise() == null) {
                recommendedExercise = null;
                lblRecTitle.setText("—");
                lblRecMeta.setText("—");
                lblRecReason.setText("—");
                btnStartRecommended.setDisable(true);
                return;
            }

            recommendedExercise = rec.exercise();
            lblRecTitle.setText(safe(recommendedExercise.getTitle(), "Exercice recommandé"));
            lblRecMeta.setText("Durée : " + recommendedExercise.getDurationMinutes() + " min • Niveau : " + recommendedExercise.getLevel());
            lblRecReason.setText(safe(rec.reason(), "Recommandation basée sur votre activité récente."));
            btnStartRecommended.setDisable(false);

        } catch (Exception e) {
            recommendedExercise = null;
            lblRecTitle.setText("—");
            lblRecMeta.setText("—");
            lblRecReason.setText("Impossible de générer une recommandation.");
            btnStartRecommended.setDisable(true);
        }
    }

    // ===================== Template Host =====================

    private void setContent(Parent page) {
        StackPane host = (StackPane) cardsPane.getScene().lookup("#contentHost");
        if (host == null) throw new IllegalStateException("contentHost introuvable dans Template.fxml");
        host.getChildren().setAll(page);
    }

    private String safe(String s, String def) {
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