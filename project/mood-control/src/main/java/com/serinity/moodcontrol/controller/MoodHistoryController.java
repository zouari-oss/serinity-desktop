package com.serinity.moodcontrol.controller;

import com.serinity.moodcontrol.dao.MoodEntryDao;
import com.serinity.moodcontrol.dao.MoodHistoryItem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MoodHistoryController {

    @FXML private VBox timelineBox;
    @FXML private ComboBox<String> rangeBox;
    @FXML private ComboBox<String> typeBox;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // Host injected by MoodHomeController
    private StackPane moodHost;

    // Keep current items for UI delete (we’ll make DB delete next step)
    private List<MoodCardVM> currentItems = new ArrayList<>();

    @FXML
    public void initialize() {
        rangeBox.getItems().setAll("Last 7 days", "Last 30 days", "All");
        rangeBox.getSelectionModel().select(0);

        typeBox.getItems().setAll("All", "Moment", "Day");
        typeBox.getSelectionModel().select(0);

        rangeBox.setOnAction(e -> loadAndRenderFromDb());
        typeBox.setOnAction(e -> loadAndRenderFromDb());

        loadAndRenderFromDb();
    }

    public void setMoodHost(StackPane moodHost) {
        this.moodHost = moodHost;
    }

    @FXML
    private void onRefresh() {
        loadAndRenderFromDb();
    }

    @FXML
    private void onLogNew() {
        try {
            Parent wizard = FXMLLoader.load(getClass().getResource("/fxml/mood/Wizard.fxml"));
            moodHost.getChildren().setAll(wizard);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadAndRenderFromDb() {
        try {
            long userId = 1L; // TEMP until user integration

            Integer lastDays = null;

            String range = rangeBox.getValue();
            if ("Last 7 days".equals(range)) {
                lastDays = 7;
            } else if ("Last 30 days".equals(range)) {
                lastDays = 30;
            } else {
                lastDays = null; // All
            }

            String typeFilter = typeBox.getValue(); // "All" / "Moment" / "Day"

            List<MoodHistoryItem> data = new MoodEntryDao().findHistory(userId, lastDays, typeFilter);

            List<MoodCardVM> vms = new ArrayList<>();
            for (MoodHistoryItem it : data) {
                vms.add(new MoodCardVM(
                        it.getId(),
                        it.getDateTime(),
                        it.getMomentType(),   // "MOMENT"/"DAY"
                        it.getMoodLevel(),
                        it.getEmotions(),
                        it.getInfluences()
                ));
            }

            currentItems = vms;
            renderTimeline(currentItems);

        } catch (Exception e) {
            e.printStackTrace();
            currentItems = new ArrayList<>();
            renderTimeline(currentItems);
        }
    }

    // ---------- Rendering ----------

    private void renderTimeline(List<MoodCardVM> items) {
        timelineBox.getChildren().clear();

        List<MoodCardVM> sorted = new ArrayList<>(items);
        sorted.sort(Comparator.comparing((MoodCardVM m) -> m.dateTime).reversed());

        Map<LocalDate, List<MoodCardVM>> grouped = new LinkedHashMap<>();
        for (MoodCardVM m : sorted) {
            grouped.computeIfAbsent(m.dateTime.toLocalDate(), k -> new ArrayList<>()).add(m);
        }

        for (Map.Entry<LocalDate, List<MoodCardVM>> entry : grouped.entrySet()) {
            LocalDate date = entry.getKey();
            timelineBox.getChildren().add(dateHeader(date));

            for (MoodCardVM m : entry.getValue()) {
                timelineBox.getChildren().add(moodCard(m));
            }
        }
    }

    private Node dateHeader(LocalDate date) {
        String title;
        LocalDate today = LocalDate.now();
        if (date.equals(today)) title = "Today";
        else if (date.equals(today.minusDays(1))) title = "Yesterday";
        else title = date.toString();

        Label lbl = new Label(title);
        lbl.getStyleClass().add("history-date");
        return lbl;
    }

    private Node moodCard(MoodCardVM m) {
        VBox card = new VBox(10);
        card.getStyleClass().add("history-card");

        HBox top = new HBox(12);
        top.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Circle c = new Circle(16);
        c.getStyleClass().add("mood-dot");
        c.setFill(colorForMood(m.moodLevel));

        VBox main = new VBox(4);

        String type = m.momentType.equals("DAY") ? "Day" : "Moment";
        String time = m.momentType.equals("DAY") ? "" : (" • " + TIME_FMT.format(m.dateTime));
        Label title = new Label(type + time);
        title.getStyleClass().add("history-card-title");

        Label sub = new Label("Mood: " + moodLabel(m.moodLevel));
        sub.getStyleClass().add("history-card-sub");

        main.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Actions (Edit / Delete)
        HBox actions = new HBox(8);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        javafx.scene.control.Button btnEdit = new javafx.scene.control.Button("✎");
        btnEdit.getStyleClass().addAll("icon-btn", "icon-btn-edit");
        btnEdit.setOnAction(e -> {
            e.consume();
            onEdit(m);
        });

        javafx.scene.control.Button btnDelete = new javafx.scene.control.Button("🗑");
        btnDelete.getStyleClass().addAll("icon-btn", "icon-btn-delete");
        btnDelete.setOnAction(e -> {
            e.consume();
            onDelete(m);
        });

        actions.getChildren().addAll(btnEdit, btnDelete);

        Label chevron = new Label("⌄");
        chevron.getStyleClass().add("history-chevron");

        top.getChildren().addAll(c, main, spacer, actions, chevron);

        FlowPane preview = new FlowPane(8, 8);
        preview.getStyleClass().add("history-tags");

        addPreviewTags(preview, m.emotions, 3, "emotion-tag");
        addPreviewTags(preview, m.influences, 2, "influence-tag");

        VBox details = new VBox(10);
        details.getStyleClass().add("history-details");

        details.getChildren().addAll(
                fullTagBlock("Emotions", m.emotions, "emotion-tag"),
                fullTagBlock("Influences", m.influences, "influence-tag")
        );

        collapse(details);

        card.setOnMouseClicked(e -> {
            boolean isCollapsed = !details.isVisible();
            if (isCollapsed) {
                chevron.setText("⌃");
                expandAnimated(details);
            } else {
                chevron.setText("⌄");
                collapseAnimated(details);
            }
        });

        card.getChildren().addAll(top, preview, details);
        return card;
    }

    private void addPreviewTags(FlowPane pane, List<String> items, int max, String styleClass) {
        int count = 0;
        for (String s : items) {
            if (count >= max) break;
            Label tag = new Label(s);
            tag.getStyleClass().addAll("tag", styleClass);
            pane.getChildren().add(tag);
            count++;
        }
        int remaining = items.size() - max;
        if (remaining > 0) {
            Label more = new Label("+" + remaining);
            more.getStyleClass().addAll("tag", "tag-more");
            pane.getChildren().add(more);
        }
    }

    private Node fullTagBlock(String title, List<String> items, String styleClass) {
        VBox box = new VBox(8);

        Label t = new Label(title);
        t.getStyleClass().add("history-details-title");

        FlowPane tags = new FlowPane(8, 8);
        for (String s : items) {
            Label tag = new Label(s);
            tag.getStyleClass().addAll("tag", styleClass);
            tags.getChildren().add(tag);
        }

        box.getChildren().addAll(t, tags);
        return box;
    }

    // ---------- Expand / Collapse (smooth) ----------

    private void collapse(Node n) {
        n.setVisible(false);
        n.setManaged(false);
        n.setOpacity(0);

        if (n instanceof Region) {
            Region r = (Region) n;
            r.setMaxHeight(0);
        }
    }

    private void expandAnimated(Node n) {
        n.setVisible(true);
        n.setManaged(true);

        Region r = (Region) n;
        r.setMaxHeight(0);
        n.applyCss();
        n.autosize();

        double target = Math.max(120, r.prefHeight(-1));

        Timeline height = new Timeline(
                new KeyFrame(Duration.millis(200),
                        new KeyValue(r.maxHeightProperty(), target, Interpolator.EASE_OUT),
                        new KeyValue(n.opacityProperty(), 1, Interpolator.EASE_OUT)
                )
        );
        n.setOpacity(0);
        height.play();
    }

    private void collapseAnimated(Node n) {
        Region r = (Region) n;

        Timeline height = new Timeline(
                new KeyFrame(Duration.millis(160),
                        new KeyValue(r.maxHeightProperty(), 0, Interpolator.EASE_IN),
                        new KeyValue(n.opacityProperty(), 0, Interpolator.EASE_IN)
                )
        );

        height.setOnFinished(e -> collapse(n));
        height.play();
    }

    // ---------- Mood helpers ----------

    private Color colorForMood(int level) {
        switch (level) {
            case 1: return Color.web("#6B7C93");
            case 2: return Color.web("#4F8FB8");
            case 3: return Color.web("#7A8C8C");
            case 4: return Color.web("#62B48F");
            case 5: return Color.web("#B9C56A");
            default: return Color.web("#7A8C8C");
        }
    }

    private String moodLabel(int level) {
        switch (level) {
            case 1: return "Very Low";
            case 2: return "Low";
            case 3: return "Neutral";
            case 4: return "Good";
            case 5: return "Very Good";
            default: return "Neutral";
        }
    }

    // ---------- Actions ----------

    private void onEdit(MoodCardVM m) {
        try {
            long userId = 1L;

            MoodEntryDao dao = new MoodEntryDao();
            MoodHistoryItem it = dao.findById(m.id, userId);
            if (it == null) return;

            // build MoodEntry model from history item
            com.serinity.moodcontrol.model.MoodEntry entry = new com.serinity.moodcontrol.model.MoodEntry();
            entry.setId(it.getId());
            entry.setUserId(userId);
            entry.setMomentType(it.getMomentType());
            entry.setMoodLevel(it.getMoodLevel());
            entry.setEmotions(new ArrayList<>(it.getEmotions()));
            entry.setInfluences(new ArrayList<>(it.getInfluences()));

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mood/Wizard.fxml"));
            Parent wizard = loader.load();

            StateOfMindWizardController wiz = loader.getController();
            wiz.startEdit(entry);

            // after finish, come back here and refresh list
            wiz.setOnFinish(new Runnable() {
                @Override
                public void run() {
                    loadAndRenderFromDb();
                }
            });

            moodHost.getChildren().setAll(wizard);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void onDelete(MoodCardVM m) {
        javafx.scene.control.Alert alert =
                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);

        alert.setTitle("Delete mood entry");
        alert.setHeaderText("Delete this entry?");
        alert.setContentText("This will remove the mood entry and its emotions/influences.");

        Optional<javafx.scene.control.ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == javafx.scene.control.ButtonType.OK) {

            // Still UI-only delete for now (next step we make DB delete real)
            currentItems.removeIf(x -> x.id == m.id);
            renderTimeline(currentItems);
        }
    }

    // ---------- VM ----------

    private static class MoodCardVM {
        final long id;
        final LocalDateTime dateTime;
        final String momentType; // "MOMENT"/"DAY"
        final int moodLevel;
        final List<String> emotions;
        final List<String> influences;

        MoodCardVM(long id, LocalDateTime dateTime, String momentType, int moodLevel,
                   List<String> emotions, List<String> influences) {
            this.id = id;
            this.dateTime = dateTime;
            this.momentType = momentType;
            this.moodLevel = moodLevel;
            this.emotions = emotions;
            this.influences = influences;
        }
    }
}
