package com.serinity.accesscontrol.controller.exercice;

import com.serinity.accesscontrol.dao.exercice.ExerciseSessionDao;
import com.serinity.accesscontrol.model.exercice.Exercise;
import com.serinity.accesscontrol.service.exercice.ai.CoachAnswer;
import com.serinity.accesscontrol.service.exercice.ai.GeminiCoachService;
import com.serinity.accesscontrol.service.exercice.performance.PerformanceAnalyzer;
import com.serinity.accesscontrol.service.exercice.performance.PerformanceReport;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class CoachDashboardController {

    @FXML private ComboBox<Exercise> cbExercise;
    @FXML private Spinner<Integer> daysSpinner;
    @FXML private Button btnAnalyze;
    @FXML private Button btnCoach;
    @FXML private ProgressIndicator loading;

    @FXML private Label lblStatus;
    @FXML private Label lblTrend;
    @FXML private Label lblSessions;
    @FXML private Label lblAvg;
    @FXML private Label lblFlags;
    @FXML private ListView<String> lvRecs;
    @FXML private Label lblNextPlan;

    @FXML private Label lblCoachSummary;
    @FXML private VBox actionsBox;
    @FXML private TextArea taCoachNextSession;
    @FXML private Label lblCoachSafety;

    @FXML private Label lblStatusBar;

    private final ExerciseSessionDao sessionDao = new ExerciseSessionDao();
    private final PerformanceAnalyzer analyzer = new PerformanceAnalyzer();
    private final GeminiCoachService gemini = new GeminiCoachService();

    private int userId = 1;
    private List<Exercise> exercises = new ArrayList<>();


    private StackPane contentHost;


    private Runnable onClose;

    private record CoachResult(PerformanceReport report, CoachAnswer answer) {}

    @FXML
    public void initialize() {
        daysSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(7, 90, 28));
        clearUI();

        // wrap propre pour recommandations
        lvRecs.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>() {
                private final Label label = new Label();
                {
                    label.setWrapText(true);
                    label.setMaxWidth(Double.MAX_VALUE);
                }
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) setGraphic(null);
                    else {
                        label.setText(item);
                        setGraphic(label);
                    }
                }
            };
            cell.setPrefWidth(0);
            return cell;
        });
    }

    public void setHost(StackPane contentHost) {
        this.contentHost = contentHost;
    }

    /**
     * Contexte
     * @param userId user
     * @param allExercises liste exercices
     * @param onClose callback optionnel (peut être null)
     */
    public void setContext(int userId, List<Exercise> allExercises, Runnable onClose) {
        this.userId = userId;
        this.exercises = allExercises == null ? new ArrayList<>() : new ArrayList<>(allExercises);
        this.onClose = onClose;

        cbExercise.setItems(FXCollections.observableArrayList(exercises));
        cbExercise.setPromptText("Global (tous les exercices)");

        cbExercise.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Exercise item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : safe(item.getTitle(), "Exercice"));
            }
        });

        cbExercise.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Exercise item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item == null ? "Global (tous les exercices)" : safe(item.getTitle(), "Exercice")));
            }
        });

        cbExercise.setValue(null); // global par défaut
    }

    @FXML
    private void onAnalyze() {
        Integer exerciseId = (cbExercise.getValue() == null) ? null : cbExercise.getValue().getId();
        int days = daysSpinner.getValue();

        setLoading(true, "Analyse en cours…");

        CompletableFuture.supplyAsync(() -> {
            try {
                var sessions = sessionDao.findRecent(userId, exerciseId, days, 250);
                return analyzer.analyze(userId, exerciseId, days, sessions);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).whenComplete((report, err) -> Platform.runLater(() -> {
            setLoading(false, null);
            if (err != null) {
                showError("Analyse impossible", rootMsg(err));
                return;
            }
            renderReport(report);
            lblStatusBar.setText("Analyse prête.");
        }));
    }

    @FXML
    private void onCoach() {
        Integer exerciseId = (cbExercise.getValue() == null) ? null : cbExercise.getValue().getId();
        int days = daysSpinner.getValue();

        setLoading(true, "Coach Gemini en cours…");

        CompletableFuture.supplyAsync(() -> {
            try {
                var sessions = sessionDao.findRecent(userId, exerciseId, days, 250);
                PerformanceReport report = analyzer.analyze(userId, exerciseId, days, sessions);
                CoachAnswer answer = gemini.callCoach(report);
                return new CoachResult(report, answer);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).whenComplete((result, err) -> Platform.runLater(() -> {
            setLoading(false, null);
            if (err != null) {
                showError("Coach impossible", rootMsg(err));
                return;
            }
            renderReport(result.report());
            renderCoach(result.answer());
            lblStatusBar.setText("Coach prêt.");
        }));
    }


    @FXML
    private void onClose() {
        if (onClose != null) {
            onClose.run();
            return;
        }

        if (contentHost == null) {

            lblStatusBar.setText("Retour impossible (contentHost null).");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/exercice/ExerciseList.fxml"));
            Parent backRoot = loader.load();
            contentHost.getChildren().setAll(backRoot);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de revenir à la liste.\n" + e.getMessage());
        }
    }

    // -------- UI helpers --------

    private void setLoading(boolean v, String msg) {
        if (loading != null) loading.setVisible(v);
        if (btnAnalyze != null) btnAnalyze.setDisable(v);
        if (btnCoach != null) btnCoach.setDisable(v);
        if (msg != null && lblStatusBar != null) lblStatusBar.setText(msg);
    }

    private void clearUI() {
        if (lblStatus != null) lblStatus.setText("—");
        if (lblTrend != null) lblTrend.setText("—");
        if (lblSessions != null) lblSessions.setText("—");
        if (lblAvg != null) lblAvg.setText("—");
        if (lblFlags != null) lblFlags.setText("—");

        if (lvRecs != null) lvRecs.setItems(FXCollections.observableArrayList());
        if (lblNextPlan != null) lblNextPlan.setText("—");

        if (lblCoachSummary != null) lblCoachSummary.setText("—");
        if (actionsBox != null) actionsBox.getChildren().clear();
        if (taCoachNextSession != null) taCoachNextSession.clear();
        if (lblCoachSafety != null) lblCoachSafety.setText("");

        if (lblStatusBar != null) lblStatusBar.setText("Prêt.");
    }

    private void renderReport(PerformanceReport r) {
        lblStatus.setText(String.valueOf(r.status()));
        lblTrend.setText(r.trendDeltaPercent() + "% (récent vs précédent)");
        lblSessions.setText(r.sessionsCompleted() + "/" + r.sessionsTotal() + " (" + r.completionRatePercent() + "%)");
        lblAvg.setText(r.avgActiveSeconds() + " sec");
        lblFlags.setText("Fatigue: " + (r.fatigueFlag() ? "oui" : "non") + " • Douleur: " + (r.painFlag() ? "oui" : "non"));


        lblStatus.getStyleClass().removeAll("coach-status-green", "coach-status-orange", "coach-status-red");
        switch (String.valueOf(r.status())) {
            case "PROGRESSION" -> lblStatus.getStyleClass().add("coach-status-green");
            case "STAGNATION"  -> lblStatus.getStyleClass().add("coach-status-orange");
            case "REGRESSION"  -> lblStatus.getStyleClass().add("coach-status-red");
            default -> {}
        }

        lvRecs.setItems(FXCollections.observableArrayList(
                r.recommendations().stream()
                        .filter(Objects::nonNull)
                        .map(x -> "• " + x.details())
                        .toList()
        ));

        var p = r.nextSessionPlan();
        lblNextPlan.setText(
                p.objective()
                        + " — cible " + p.targetActiveSeconds() + " sec"
                        + (p.targetReps() != null ? ", reps: " + p.targetReps() : "")
                        + (p.targetRpe() != null ? ", RPE: " + p.targetRpe() : "")
        );
    }

    private void renderCoach(CoachAnswer a) {
        lblCoachSummary.setText(a.summary());

        actionsBox.getChildren().clear();
        for (String act : a.actions()) {
            Label l = new Label("• " + act);
            l.getStyleClass().add("coach-action-text");
            l.setWrapText(true);

            VBox card = new VBox(l);
            card.getStyleClass().add("coach-action-card");
            actionsBox.getChildren().add(card);
        }

        var ns = a.nextSession();
        taCoachNextSession.setText(
                "Échauffement:\n" + ns.warmup()
                        + "\n\nSéance:\n" + ns.main()
                        + "\n\nRetour au calme:\n" + ns.cooldown()
        );

        lblCoachSafety.setText(a.safetyNote());
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
        if (lblStatusBar != null) lblStatusBar.setText("Erreur.");
    }

    private static String safe(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }

    private static String rootMsg(Throwable t) {
        Throwable x = t;
        while (x.getCause() != null) x = x.getCause();
        return x.getMessage() == null ? x.toString() : x.getMessage();
    }
}
