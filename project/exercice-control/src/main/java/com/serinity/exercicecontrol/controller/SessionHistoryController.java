package com.serinity.exercicecontrol.controller;

import com.serinity.exercicecontrol.dao.DbConnection;
import com.serinity.exercicecontrol.model.Exercise;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SessionHistoryController {

    @FXML private Label lblTitle;
    @FXML private Label lblCount;

    @FXML private TableView<SessionRow> table;
    @FXML private TableColumn<SessionRow, Integer> colId;
    @FXML private TableColumn<SessionRow, String> colStatus;
    @FXML private TableColumn<SessionRow, LocalDateTime> colStarted;
    @FXML private TableColumn<SessionRow, LocalDateTime> colCompleted;
    @FXML private TableColumn<SessionRow, String> colFeedback;

    private Exercise exercise;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void setExercise(Exercise ex) {
        this.exercise = ex;
        String title = (ex != null && ex.getTitle() != null) ? ex.getTitle() : "(Sans titre)";
        lblTitle.setText("Historique — " + title);
        refresh();
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> data.getValue().idProperty().asObject());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        colStarted.setCellValueFactory(data -> data.getValue().startedAtProperty());
        colCompleted.setCellValueFactory(data -> data.getValue().completedAtProperty());
        colFeedback.setCellValueFactory(data -> data.getValue().feedbackProperty());

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Aucune session pour cet exercice pour le moment."));

        // Status pill rendering
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                String s = status.trim().toUpperCase();
                Label pill = new Label(s);
                pill.getStyleClass().add("pill");

                switch (s) {
                    case "COMPLETED" -> pill.getStyleClass().add("pill-success");
                    case "IN_PROGRESS" -> pill.getStyleClass().add("pill-info");
                    default -> pill.getStyleClass().add("pill-warn");
                }

                setText(null);
                setGraphic(pill);
            }
        });

        // Date formatting
        colStarted.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "—" : item.format(fmt));
            }
        });

        colCompleted.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "—" : item.format(fmt));
            }
        });


        colFeedback.setCellFactory(col -> new TableCell<>() {
            private final Label wrap = new Label();
            {
                wrap.setWrapText(true);
                wrap.setMaxWidth(Double.MAX_VALUE);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.isBlank()) {
                    setText("—");
                    setGraphic(null);
                } else {
                    wrap.setText(item);
                    setText(null);
                    setGraphic(wrap);
                }
            }
        });
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    private void refresh() {
        if (exercise == null) return;

        ObservableList<SessionRow> rows = FXCollections.observableArrayList();

        String sql = """
                SELECT id, status, started_at, completed_at, feedback
                FROM exercise_session
                WHERE exercise_id = ?
                ORDER BY started_at DESC
                """;


        Connection cnx = DbConnection.getInstance().getConnection();

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, exercise.getId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String status = rs.getString("status");
                    Timestamp st = rs.getTimestamp("started_at");
                    Timestamp ct = rs.getTimestamp("completed_at");
                    String fb = rs.getString("feedback");

                    rows.add(new SessionRow(
                            id,
                            status,
                            st != null ? st.toLocalDateTime() : null,
                            ct != null ? ct.toLocalDateTime() : null,
                            fb
                    ));
                }
            }

            table.setItems(rows);
            if (lblCount != null) lblCount.setText("(" + rows.size() + ")");

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur BD", "Impossible de charger l'historique.\n" + e.getMessage());
        }
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
        StackPane host = (StackPane) table.getScene().lookup("#contentHost");
        if (host == null) {
            throw new IllegalStateException("contentHost introuvable. Vérifie fx:id=\"contentHost\" dans Template.fxml");
        }
        host.getChildren().setAll(page);
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
