package com.serinity.exercicecontrol.controller;

import com.serinity.exercicecontrol.dao.DbConnection;
import com.serinity.exercicecontrol.model.Exercise;
import com.serinity.exercicecontrol.model.ExerciseSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SessionHistoryController {

    @FXML private Label lblTitle;
    @FXML private Label lblCount;
    @FXML private ListView<ExerciseSession> historyList;

    private Exercise exercise;
    private final ObservableList<ExerciseSession> sessions = FXCollections.observableArrayList();

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // =========================
    // INITIALIZATION
    // =========================

    @FXML
    public void initialize() {

        historyList.setItems(sessions);
        historyList.setPlaceholder(new Label("Aucune session pour cet exercice pour le moment."));

        historyList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(ExerciseSession s, boolean empty) {
                super.updateItem(s, empty);

                if (empty || s == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                // ===== TITLE =====
                String title = (exercise != null && exercise.getTitle() != null)
                        ? exercise.getTitle()
                        : "Exercice";

                Label titleLbl = new Label(title);
                titleLbl.getStyleClass().add("card-title");

                // ===== STATUS (FR) =====
                String raw = (s.getStatus() == null)
                        ? ""
                        : s.getStatus().trim().toUpperCase();

                String statusFr = switch (raw) {
                    case "COMPLETED"   -> "TERMINÉ";
                    case "IN_PROGRESS" -> "EN COURS";
                    case "CANCELLED"   -> "ANNULÉ";
                    case "FAILED"      -> "ÉCHOUÉ";
                    default            -> raw.isBlank() ? "—" : raw;
                };

                Label pill = new Label(statusFr);
                pill.getStyleClass().add("pill");

                switch (raw) {
                    case "COMPLETED"   -> pill.getStyleClass().add("pill-success");
                    case "IN_PROGRESS" -> pill.getStyleClass().add("pill-info");
                    default            -> pill.getStyleClass().add("pill-warn");
                }

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox topRow = new HBox(12, titleLbl, spacer, pill);

                // ===== DATES =====
                String started = (s.getStartedAt() == null)
                        ? "—"
                        : s.getStartedAt().format(fmt);

                String completed = (s.getCompletedAt() == null)
                        ? "—"
                        : s.getCompletedAt().format(fmt);

                Label dates = new Label("Début : " + started + "  •  Fin : " + completed);
                dates.getStyleClass().add("card-sub");

                // ===== FEEDBACK =====
                String fb = (s.getFeedback() == null || s.getFeedback().isBlank())
                        ? "Aucun feedback"
                        : s.getFeedback();

                Label feedback = new Label("Feedback : " + fb);
                feedback.getStyleClass().add("card-sub");
                feedback.setWrapText(true);

                VBox card = new VBox(8, topRow, dates, feedback);
                card.getStyleClass().add("history-card");

                setText(null);
                setGraphic(card);
            }
        });
    }

    // =========================
    // SET EXERCISE
    // =========================

    public void setExercise(Exercise ex) {
        this.exercise = ex;

        String title = (ex != null && ex.getTitle() != null)
                ? ex.getTitle()
                : "(Sans titre)";

        lblTitle.setText("Historique — " + title);
        refresh();
    }

    // =========================
    // REFRESH DATA
    // =========================

    @FXML
    private void onRefresh() {
        refresh();
    }

    private void refresh() {

        if (exercise == null) return;

        sessions.clear();

        String sql = """
                SELECT id, user_id, exercise_id, status, started_at, completed_at, feedback
                FROM exercise_session
                WHERE exercise_id = ?
                ORDER BY started_at DESC
                """;

        try {

            // IMPORTANT: do NOT close singleton connection
            Connection cnx = DbConnection.getInstance().getConnection();

            try (PreparedStatement ps = cnx.prepareStatement(sql)) {

                ps.setInt(1, exercise.getId());

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        int id = rs.getInt("id");
                        int userId = rs.getInt("user_id");
                        int exerciseId = rs.getInt("exercise_id");
                        String status = rs.getString("status");

                        Timestamp st = rs.getTimestamp("started_at");
                        Timestamp ct = rs.getTimestamp("completed_at");
                        String fb = rs.getString("feedback");

                        LocalDateTime startedAt = (st != null)
                                ? st.toLocalDateTime()
                                : null;

                        LocalDateTime completedAt = (ct != null)
                                ? ct.toLocalDateTime()
                                : null;

                        sessions.add(new ExerciseSession(
                                id,
                                userId,
                                exerciseId,
                                status,
                                startedAt,
                                completedAt,
                                fb
                        ));
                    }
                }
            }

            lblCount.setText("(" + sessions.size() + ")");

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur BD",
                    "Impossible de charger l'historique.\n" + e.getMessage());
        }
    }

    // =========================
    // NAVIGATION
    // =========================

    @FXML
    private void onBack() {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/exercice/ExerciseDetails.fxml")
            );

            Parent root = loader.load();

            ExerciseDetailsController ctrl = loader.getController();
            ctrl.setExercise(exercise);

            setContent(root);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur",
                    "Impossible de revenir aux détails.");
        }
    }

    private void setContent(Parent page) {
        StackPane host = (StackPane)
                historyList.getScene().lookup("#contentHost");

        if (host == null) {
            throw new IllegalStateException(
                    "contentHost introuvable. Vérifie fx:id=\"contentHost\" dans Template.fxml"
            );
        }

        host.getChildren().setAll(page);
    }

    // =========================
    // ERROR DIALOG
    // =========================

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}