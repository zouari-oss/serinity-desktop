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

    @FXML private ResourceBundle resources;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // Host injected by MoodHomeController
    private StackPane moodHost;

    // Keep current items for UI delete (we’ll make DB delete next step)
    private List<MoodCardVM> currentItems = new ArrayList<MoodCardVM>();

    @FXML
    public void initialize() {
        if (resources == null) {
            throw new IllegalStateException("ResourceBundle not injected in MoodHistoryController. Load MoodHistory.fxml with bundle.");
        }

        // Range options (localized labels, but we map them to numeric days)
        rangeBox.getItems().setAll(
                t("history.range.last7"),
                t("history.range.last30"),
                t("history.range.all")
        );
        rangeBox.getSelectionModel().select(0);

        // Type options (localized labels, but we map them to ALL/MOMENT/DAY codes)
        typeBox.getItems().setAll(
                t("history.type.all"),
                t("history.type.moment"),
                t("history.type.day")
        );
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mood/Wizard.fxml"), resources);
            Parent wizard = loader.load();

            // ✅ IMPORTANT: inject host so Finish can navigate to history
            StateOfMindWizardController wiz = loader.getController();
            wiz.setMoodHost(moodHost);

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
            if (t("history.range.last7").equals(range)) {
                lastDays = Integer.valueOf(7);
            } else if (t("history.range.last30").equals(range)) {
                lastDays = Integer.valueOf(30);
            } else {
                lastDays = null; // All
            }

            // Map localized label -> CODE expected by DAO
            String typeFilter = toTypeCode(typeBox.getValue()); // ALL/MOMENT/DAY

            List<MoodHistoryItem> data = new MoodEntryDao().findHistory(userId, lastDays, typeFilter);

            List<MoodCardVM> vms = new ArrayList<MoodCardVM>();
            for (MoodHistoryItem it : data) {
                vms.add(new MoodCardVM(
                        it.getId(),
                        it.getDateTime(),
                        it.getMomentType(),   // "MOMENT"/"DAY"
                        it.getMoodLevel(),
                        it.getEmotions(),     // CODES
                        it.getInfluences()    // CODES
                ));
            }

            currentItems = vms;
            renderTimeline(currentItems);

        } catch (Exception e) {
            e.printStackTrace();
            currentItems = new ArrayList<MoodCardVM>();
            renderTimeline(currentItems);
        }
    }

    // ---------- Rendering ----------

    private void renderTimeline(List<MoodCardVM> items) {
        timelineBox.getChildren().clear();

        List<MoodCardVM> sorted = new ArrayList<MoodCardVM>(items);
        sorted.sort(new Comparator<MoodCardVM>() {
            @Override
            public int compare(MoodCardVM a, MoodCardVM b) {
                return b.dateTime.compareTo(a.dateTime);
            }
        });

        Map<LocalDate, List<MoodCardVM>> grouped = new LinkedHashMap<LocalDate, List<MoodCardVM>>();
        for (MoodCardVM m : sorted) {
            LocalDate d = m.dateTime.toLocalDate();
            if (!grouped.containsKey(d)) grouped.put(d, new ArrayList<MoodCardVM>());
            grouped.get(d).add(m);
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

        if (date.equals(today)) title = t("history.today");
        else if (date.equals(today.minusDays(1))) title = t("history.yesterday");
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

        String type = "DAY".equals(m.momentType) ? t("history.type.day") : t("history.type.moment");
        String time = "DAY".equals(m.momentType) ? "" : (" • " + TIME_FMT.format(m.dateTime));
        Label title = new Label(type + time);
        title.getStyleClass().add("history-card-title");

        Label sub = new Label(t("history.mood.prefix") + " " + moodLabel(m.moodLevel));
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

        addPreviewTags(preview, m.emotions, 3, "emotion-tag", "emotion.");
        addPreviewTags(preview, m.influences, 2, "influence-tag", "influence.");

        VBox details = new VBox(10);
        details.getStyleClass().add("history-details");

        details.getChildren().addAll(
                fullTagBlock(t("history.details.emotions"), m.emotions, "emotion-tag", "emotion."),
                fullTagBlock(t("history.details.influences"), m.influences, "influence-tag", "influence.")
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

    private void addPreviewTags(FlowPane pane, List<String> items, int max, String styleClass, String keyPrefix) {
        int count = 0;
        for (String s : items) {
            if (count >= max) break;
            Label tag = new Label(t(keyPrefix + s));
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

    private Node fullTagBlock(String title, List<String> items, String styleClass, String keyPrefix) {
        VBox box = new VBox(8);

        Label t = new Label(title);
        t.getStyleClass().add("history-details-title");

        FlowPane tags = new FlowPane(8, 8);
        for (String s : items) {
            Label tag = new Label(t(keyPrefix + s));
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
        return t("mood.level." + level);
    }

    // ---------- Actions ----------

    private void onEdit(MoodCardVM m) {
        try {
            long userId = 1L;

            MoodEntryDao dao = new MoodEntryDao();
            MoodHistoryItem it = dao.findById(m.id, userId);
            if (it == null) return;

            com.serinity.moodcontrol.model.MoodEntry entry = new com.serinity.moodcontrol.model.MoodEntry();
            entry.setId(it.getId());
            entry.setUserId(userId);
            entry.setMomentType(it.getMomentType());
            entry.setMoodLevel(it.getMoodLevel());
            entry.setEmotions(new ArrayList<String>(it.getEmotions()));       // CODES
            entry.setInfluences(new ArrayList<String>(it.getInfluences()));   // CODES

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mood/Wizard.fxml"), resources);
            Parent wizard = loader.load();

            StateOfMindWizardController wiz = loader.getController();
            wiz.setMoodHost(moodHost); // ✅ IMPORTANT: inject host so Finish can navigate
            wiz.startEdit(entry);

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

        alert.setTitle(t("history.delete.title"));
        alert.setHeaderText(t("history.delete.header"));
        alert.setContentText(t("history.delete.body"));

        Optional<javafx.scene.control.ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == javafx.scene.control.ButtonType.OK) {
            // Still UI-only delete for now (next step we make DB delete real)
            currentItems.removeIf(x -> x.id == m.id);
            renderTimeline(currentItems);
        }
    }

    // ---------- Helpers ----------

    private String t(String key) {
        try {
            return resources.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    private String toTypeCode(String label) {
        if (label == null) return "ALL";
        if (label.equals(t("history.type.moment"))) return "MOMENT";
        if (label.equals(t("history.type.day"))) return "DAY";
        return "ALL";
    }

    // ---------- VM ----------

    private static class MoodCardVM {
        final long id;
        final LocalDateTime dateTime;
        final String momentType; // "MOMENT"/"DAY"
        final int moodLevel;
        final List<String> emotions;    // CODES
        final List<String> influences;  // CODES

        MoodCardVM(long id, LocalDateTime dateTime, String momentType, int moodLevel,
                   List<String> emotions, List<String> influences) {
            this.id = id;
            this.dateTime = dateTime;
            this.momentType = momentType;
            this.moodLevel = moodLevel;
            this.emotions = emotions == null ? new ArrayList<String>() : emotions;
            this.influences = influences == null ? new ArrayList<String>() : influences;
        }
    }
}
