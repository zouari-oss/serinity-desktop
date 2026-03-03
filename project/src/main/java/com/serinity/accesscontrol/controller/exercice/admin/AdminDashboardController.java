package com.serinity.accesscontrol.controller.exercice.admin;

import com.serinity.accesscontrol.controller.exercice.ExerciseFormController;
import com.serinity.accesscontrol.dao.exercice.AdminDashboardDao;
import com.serinity.accesscontrol.dao.exercice.ExerciseSessionDao.SessionSummary;
import com.serinity.accesscontrol.model.exercice.Exercise;
import com.serinity.accesscontrol.service.exercice.ExerciseService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class AdminDashboardController {

    @FXML private Label lblStatus;

    // Exercises
    @FXML private TableView<Exercise> tvExercises;
    @FXML private TableColumn<Exercise, Integer> colExId;
    @FXML private TableColumn<Exercise, String> colExTitle;
    @FXML private TableColumn<Exercise, String> colExType;
    @FXML private TableColumn<Exercise, Integer> colExLevel;
    @FXML private TableColumn<Exercise, Integer> colExDuration;

    // Sessions filters
    @FXML private Spinner<Integer> spDays;
    @FXML private ComboBox<Exercise> cbSessionExercise;
    @FXML private ComboBox<String> cbSessionStatus;
    @FXML private TextField tfSearch;

    // Sessions table
    @FXML private TableView<SessionSummary> tvSessions;
    @FXML private TableColumn<SessionSummary, Integer> colSeId;
    @FXML private TableColumn<SessionSummary, Integer> colSeExercise;
    @FXML private TableColumn<SessionSummary, String> colSeStatus;
    @FXML private TableColumn<SessionSummary, String> colSeStart;
    @FXML private TableColumn<SessionSummary, String> colSeEnd;
    @FXML private TableColumn<SessionSummary, Integer> colSeActive;
    @FXML private TableColumn<SessionSummary, String> colSeFeedback;

    // Analytics
    @FXML private Label lblTotalSessions;
    @FXML private Label lblCompleted;
    @FXML private Label lblCompletionRate;
    @FXML private Label lblActiveTotal;
    @FXML private Label lblAvgActive;
    @FXML private ListView<String> lvTopExercises;
    @FXML private LineChart<String, Number> chartSessions;

    private final ExerciseService exerciseService = new ExerciseService();
    private final AdminDashboardDao adminDao = new AdminDashboardDao();

    private Runnable onBack;

    // TODO: remplacer par user connecté quand tu ajouteras users
    private int userId = 1;

    @FXML
    public void initialize() {
        // Spinner days
        if (spDays != null) {
            spDays.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(7, 180, 30));
            spDays.setEditable(true);
        }

        initExercisesTable();
        initSessionsTable();
        initFilters();
        initUXHelpers();

        // Double clic -> modifier (popup)
        if (tvExercises != null) {
            tvExercises.setRowFactory(tv -> {
                TableRow<Exercise> row = new TableRow<>();
                row.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2 && !row.isEmpty()) {
                        onEditSelectedExercise();
                    }
                });
                return row;
            });
        }

        onRefreshAll();
    }

    private void initUXHelpers() {
        // Enter dans la recherche -> reload sessions
        if (tfSearch != null) {
            tfSearch.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER) onLoadSessions();
            });
        }
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    // =============================
    // NAVIGATION BACK (garanti)
    // =============================
    @FXML
    private void onBack() {
        // 1) si un callback est fourni, on tente
        if (onBack != null) {
            try {
                onBack.run();
                return;
            } catch (Exception ignored) {
                // fallback si le runnable dépend d'un ancien controller
            }
        }

        // 2) fallback garanti : on recharge ExerciseList dans le contentHost du Template.fxml
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/exercice/ExerciseList.fxml"));
            Parent page = loader.load();

            StackPane host = (StackPane) lblStatus.getScene().lookup("#contentHost"); if (host == null) host = (StackPane) lblStatus.getScene().lookup("#contentHostStackPane");
            if (host == null) throw new IllegalStateException("contentHost introuvable (Template.fxml)");

            host.getChildren().setAll(page);

        } catch (Exception e) {
            fail("Retour impossible", e);
        }
    }

    // =============================
    // REFRESH ALL
    // =============================
    @FXML
    public void onRefreshAll() {
        if (lblStatus != null) lblStatus.setText("Chargement…");

        CompletableFuture.runAsync(() -> {
            try {
                List<Exercise> ex = exerciseService.getAllExercises();
                List<SessionSummary> sessions = adminDao.findRecentSessionsFiltered(
                        userId, 30, 700, null, null, null
                );

                Platform.runLater(() -> {
                    tvExercises.setItems(FXCollections.observableArrayList(ex));
                    updateExerciseFilterItems(ex);

                    tvSessions.setItems(FXCollections.observableArrayList(sessions));
                    refreshAnalyticsFromSessions(sessions, ex);

                    if (lblStatus != null) lblStatus.setText("OK.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> fail("Erreur", e));
            }
        });
    }

    // =============================
    // EXERCISES CRUD (popup)
    // =============================
    private void initExercisesTable() {
        colExId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colExTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colExType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colExLevel.setCellValueFactory(new PropertyValueFactory<>("level"));
        colExDuration.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));

        tvExercises.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void onAddExercise() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/exercice/ExerciseForm.fxml"));
            Parent root = loader.load();

            ExerciseFormController form = loader.getController();
            form.setModeCreateReturnToList(this::onRefreshAll);

            openDialogAndWait("Ajouter un exercice", root);

            // sécurité: refresh après fermeture
            onRefreshAll();

        } catch (Exception e) {
            fail("Impossible d'ouvrir le formulaire", e);
        }
    }

    @FXML
    private void onEditSelectedExercise() {
        Exercise selected = tvExercises.getSelectionModel().getSelectedItem();
        if (selected == null) {
            if (lblStatus != null) lblStatus.setText("Sélectionne un exercice.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/exercice/ExerciseForm.fxml"));
            Parent root = loader.load();

            ExerciseFormController form = loader.getController();
            form.setModeEditReturnToList(selected, this::onRefreshAll);

            openDialogAndWait("Modifier un exercice", root);

            onRefreshAll();

        } catch (Exception e) {
            fail("Impossible d'ouvrir la modification", e);
        }
    }

    @FXML
    private void onDeleteSelectedExercise() {
        Exercise selected = tvExercises.getSelectionModel().getSelectedItem();
        if (selected == null) {
            if (lblStatus != null) lblStatus.setText("Sélectionne un exercice.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer l'exercice : " + safe(selected.getTitle(), "Exercice") + " ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            exerciseService.deleteExercise(selected.getId());
            onRefreshAll();
            if (lblStatus != null) lblStatus.setText("Exercice supprimé.");
        } catch (SQLException e) {
            fail("Suppression impossible", e);
        }
    }

    // =============================
    // SESSIONS
    // =============================
    private void initFilters() {
        if (cbSessionStatus != null) {
            cbSessionStatus.setItems(FXCollections.observableArrayList(
                    "Tous", "STARTED", "COMPLETED", "ABANDONED"
            ));
            cbSessionStatus.getSelectionModel().selectFirst();
        }
        if (tfSearch != null) tfSearch.setText("");
    }

    private void updateExerciseFilterItems(List<Exercise> ex) {
        if (cbSessionExercise == null) return;

        cbSessionExercise.setItems(FXCollections.observableArrayList(ex));
        cbSessionExercise.setPromptText("Tous les exercices");

        cbSessionExercise.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Exercise item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (item.getId() + " — " + safe(item.getTitle(), "Exercice")));
            }
        });

        cbSessionExercise.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Exercise item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setText(null);
                else setText(item == null
                        ? "Tous les exercices"
                        : (item.getId() + " — " + safe(item.getTitle(), "Exercice")));
            }
        });

        cbSessionExercise.setValue(null);
    }

    private void initSessionsTable() {
        colSeId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSeExercise.setCellValueFactory(new PropertyValueFactory<>("exerciseId"));
        colSeStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colSeStart.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().startedAt() == null ? "—" : cd.getValue().startedAt().toString()
        ));
        colSeEnd.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().completedAt() == null ? "—" : cd.getValue().completedAt().toString()
        ));

        colSeActive.setCellValueFactory(new PropertyValueFactory<>("activeSeconds"));
        colSeFeedback.setCellValueFactory(new PropertyValueFactory<>("feedback"));

        tvSessions.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void onResetSessionFilters() {
        if (spDays != null) spDays.getValueFactory().setValue(30);
        if (cbSessionExercise != null) cbSessionExercise.setValue(null);
        if (cbSessionStatus != null) cbSessionStatus.getSelectionModel().selectFirst();
        if (tfSearch != null) tfSearch.setText("");
        onLoadSessions();
    }

    @FXML
    private void onLoadSessions() {
        int days = spDays != null ? spDays.getValue() : 30;

        Integer exerciseId = (cbSessionExercise == null || cbSessionExercise.getValue() == null)
                ? null : cbSessionExercise.getValue().getId();

        String status = cbSessionStatus == null ? null : cbSessionStatus.getValue();
        if (status != null && status.equalsIgnoreCase("Tous")) status = null;

        String q = (tfSearch == null) ? null : tfSearch.getText();
        if (q != null && q.isBlank()) q = null;

        if (lblStatus != null) lblStatus.setText("Chargement sessions…");

        final String finalStatus = status;
        final String finalQ = q;

        CompletableFuture.runAsync(() -> {
            try {
                List<SessionSummary> sessions = adminDao.findRecentSessionsFiltered(
                        userId, days, 1000, exerciseId, finalStatus, finalQ
                );

                List<Exercise> ex = exerciseService.getAllExercises();

                Platform.runLater(() -> {
                    tvSessions.setItems(FXCollections.observableArrayList(sessions));
                    tvExercises.setItems(FXCollections.observableArrayList(ex));
                    updateExerciseFilterItems(ex);

                    refreshAnalyticsFromSessions(sessions, ex);
                    if (lblStatus != null) lblStatus.setText("Sessions chargées.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> fail("Erreur sessions", e));
            }
        });
    }

    @FXML
    private void onDeleteSelectedSession() {
        SessionSummary selected = tvSessions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            if (lblStatus != null) lblStatus.setText("Sélectionne une session.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer la session ID=" + selected.id() + " ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            adminDao.deleteSession(selected.id());
            onLoadSessions();
            if (lblStatus != null) lblStatus.setText("Session supprimée.");
        } catch (SQLException e) {
            fail("Suppression impossible", e);
        }
    }

    @FXML
    private void onExportCsv() {
        List<SessionSummary> sessions = tvSessions.getItems();
        if (sessions == null || sessions.isEmpty()) {
            if (lblStatus != null) lblStatus.setText("Aucune session à exporter.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter sessions en CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        fc.setInitialFileName("sessions_export.csv");

        File file = fc.showSaveDialog(getStageSafe());
        if (file == null) return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("id,user_id,exercise_id,status,started_at,completed_at,active_seconds,feedback");
            bw.newLine();

            for (SessionSummary s : sessions) {
                bw.write(csv(s.id()) + "," +
                        csv(s.userId()) + "," +
                        csv(s.exerciseId()) + "," +
                        csv(s.status()) + "," +
                        csv(s.startedAt() == null ? "" : s.startedAt().toString()) + "," +
                        csv(s.completedAt() == null ? "" : s.completedAt().toString()) + "," +
                        csv(s.activeSeconds()) + "," +
                        csv(s.feedback() == null ? "" : s.feedback()));
                bw.newLine();
            }

            if (lblStatus != null) lblStatus.setText("CSV exporté : " + file.getName());
        } catch (Exception e) {
            fail("Export CSV impossible", e);
        }
    }

    private static String csv(Object v) {
        String s = String.valueOf(v).replace("\"", "\"\"");
        return "\"" + s + "\"";
    }

    // =============================
    // ANALYTICS
    // =============================
    @FXML
    private void onRefreshAnalytics() {
        List<SessionSummary> sessions = tvSessions.getItems();
        List<Exercise> ex = tvExercises.getItems();
        refreshAnalyticsFromSessions(
                sessions == null ? List.of() : new ArrayList<>(sessions),
                ex == null ? List.of() : new ArrayList<>(ex)
        );
        if (lblStatus != null) lblStatus.setText("Analytics recalculés.");
    }

    private void refreshAnalyticsFromSessions(List<SessionSummary> sessions, List<Exercise> exercises) {
        if (sessions == null) sessions = List.of();
        if (exercises == null) exercises = List.of();

        int total = sessions.size();
        int completed = (int) sessions.stream()
                .filter(s -> "COMPLETED".equalsIgnoreCase(s.status()))
                .count();
        int rate = total == 0 ? 0 : (completed * 100) / total;

        int activeTotal = sessions.stream().mapToInt(SessionSummary::activeSeconds).sum();
        int avgActive = total == 0 ? 0 : (int) Math.round(
                sessions.stream().mapToInt(SessionSummary::activeSeconds).average().orElse(0)
        );

        if (lblTotalSessions != null) lblTotalSessions.setText(String.valueOf(total));
        if (lblCompleted != null) lblCompleted.setText(String.valueOf(completed));
        if (lblCompletionRate != null) lblCompletionRate.setText(rate + "%");
        if (lblActiveTotal != null) lblActiveTotal.setText(String.valueOf(activeTotal));
        if (lblAvgActive != null) lblAvgActive.setText(String.valueOf(avgActive));

        Map<Integer, String> exTitle = exercises.stream()
                .collect(Collectors.toMap(
                        Exercise::getId,
                        e -> safe(e.getTitle(), "Exercice"),
                        (a, b) -> a
                ));

        Map<Integer, Long> byExercise = sessions.stream()
                .filter(s -> "COMPLETED".equalsIgnoreCase(s.status()))
                .collect(Collectors.groupingBy(SessionSummary::exerciseId, Collectors.counting()));

        List<String> top = byExercise.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(10)
                .map(e -> "• " + exTitle.getOrDefault(e.getKey(), "Exercise #" + e.getKey()) + " — " + e.getValue())
                .toList();

        if (lvTopExercises != null) {
            lvTopExercises.setItems(FXCollections.observableArrayList(top.isEmpty() ? List.of("—") : top));
        }

        if (chartSessions == null) return;
        chartSessions.getData().clear();

        XYChart.Series<String, Number> sTotal = new XYChart.Series<>();
        sTotal.setName("Sessions");

        XYChart.Series<String, Number> sComp = new XYChart.Series<>();
        sComp.setName("Complétées");

        Map<LocalDate, Long> perDayAll = sessions.stream()
                .map(s -> {
                    var dt = (s.startedAt() != null) ? s.startedAt() : s.completedAt();
                    return (LocalDate)(dt == null ? LocalDate.now() : dt.toLocalDate());
                })
                .collect(Collectors.groupingBy(d -> d, Collectors.counting()));

        Map<LocalDate, Long> perDayCompleted = sessions.stream()
                .filter(s -> "COMPLETED".equalsIgnoreCase(s.status()))
                .map(s -> {
                    var dt = (s.completedAt() != null) ? s.completedAt() : s.startedAt();
                    return (LocalDate)(dt == null ? LocalDate.now() : dt.toLocalDate());
                })
                .collect(Collectors.groupingBy(d -> d, Collectors.counting()));

        LocalDate today = LocalDate.now();
        for (int i = 13; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            long all = perDayAll.getOrDefault(d, 0L);
            long comp = perDayCompleted.getOrDefault(d, 0L);

            String label = d.toString().substring(5); // MM-DD
            sTotal.getData().add(new XYChart.Data<>(label, all));
            sComp.getData().add(new XYChart.Data<>(label, comp));
        }

        chartSessions.getData().addAll(sTotal, sComp);
    }

    // =============================
    // UI helpers
    // =============================
    private void openDialogAndWait(String title, Parent root) {
        Stage owner = getStageSafe();

        Stage dialog = new Stage();
        dialog.setTitle(title);
        if (owner != null) dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setResizable(false);

        Scene scene = new Scene(root);

        // Applique le CSS admin au popup (optionnel, mais propre)
        var css = getClass().getResource("/styles/exercice/adminstyle.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private Stage getStageSafe() {
        if (lblStatus != null && lblStatus.getScene() != null) return (Stage) lblStatus.getScene().getWindow();
        if (tvExercises != null && tvExercises.getScene() != null) return (Stage) tvExercises.getScene().getWindow();
        if (tvSessions != null && tvSessions.getScene() != null) return (Stage) tvSessions.getScene().getWindow();
        return null;
    }

    private void fail(String title, Throwable e) {
        if (lblStatus != null) lblStatus.setText("Erreur: " + rootMsg(e));

        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(rootMsg(e));
        a.showAndWait();
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