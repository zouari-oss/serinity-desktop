package com.serinity.moodcontrol.controller;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class JournalController {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // ----- FXML -----
    @FXML private VBox journalBox;

    @FXML private StackPane overlay;
    @FXML private StackPane editorHost;

    @FXML private ResourceBundle resources;

    private JournalEditorController editor;

    // ----- temp data (replace with DAO later) -----
    private static class JournalVM {
        String title;
        String content;      // Q1[key]\nA1:...\n...
        LocalDateTime dateTime;

        JournalVM(String title, String content, LocalDateTime dt) {
            this.title = title;
            this.content = content;
            this.dateTime = dt;
        }
    }

    // tracks current "edit context" (no id needed)
    private JournalVM editing = null;

    private final List<JournalVM> items = new ArrayList<JournalVM>();

    @FXML
    public void initialize() {
        if (resources == null) {
            throw new IllegalStateException("ResourceBundle not injected in JournalController. Load Journal.fxml with a bundle.");
        }

        loadEditor();
        seed();
        render();
        closeEditorInstant();
    }

    // ======================
    // Load editor component
    // ======================

    private void loadEditor() {
        try {
            final FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/mood/JournalEditor.fxml"),
                    resources
            );

            final Parent view = loader.load();
            editor = loader.getController();

            editor.setOnSave(draft -> onEditorSave(draft));
            editor.setOnCancel(new Runnable() {
                @Override public void run() { closeEditor(); }
            });

            editorHost.getChildren().setAll(view);

        } catch (final Exception e) {
            throw new RuntimeException("Failed to load /fxml/mood/JournalEditor.fxml", e);
        }
    }

    // ======================
    // Top actions
    // ======================

    @FXML
    private void onNew() {
        editing = null;

        editor.openNew(
                t("journal.editor.new"),
                t("journal.editor.subtitle")
        );

        showEditor();
    }

    // ======================
    // Editor callbacks
    // ======================

    private void onEditorSave(final JournalEditorController.JournalDraft d) {
        String title = safe(d.title);
        if (title.isEmpty()) title = t("journal.untitled");

        final String content = serializeGuided(d.a1, d.a2, d.a3);

        if (editing == null) {
            items.add(0, new JournalVM(title, content, LocalDateTime.now()));
        } else {
            editing.title = title;
            editing.content = content;
            // keep original datetime (editing shouldn’t change time)
        }

        render();
        closeEditor();
    }

    private void openEdit(final JournalVM vm) {
        editing = vm;

        final Parsed p = parseGuided(vm.content);

        editor.openEdit(
                t("journal.editor.edit"),
                t("journal.editor.subtitle"),
                vm.title,
                p.a1, p.a2, p.a3
        );

        showEditor();
    }

    // ======================
    // Rendering
    // ======================

    private void render() {
        journalBox.getChildren().clear();

        if (items.isEmpty()) {
            journalBox.getChildren().add(emptyState());
            return;
        }

        LocalDate current = null;
        for (final JournalVM vm : items) {
            final LocalDate d = vm.dateTime.toLocalDate();
            if (current == null || !current.equals(d)) {
                current = d;
                journalBox.getChildren().add(dateHeader(d));
            }
            journalBox.getChildren().add(journalCard(vm));
        }
    }

    private Node emptyState() {
        final VBox box = new VBox(10);
        box.getStyleClass().add("history-card");

        final Label t1 = new Label(t("journal.empty.title"));
        t1.getStyleClass().add("history-card-title");

        final Label t2 = new Label(t("journal.empty.sub"));
        t2.getStyleClass().add("history-card-sub");

        final Button b = new Button(t("journal.new"));
        b.getStyleClass().add("btn-primary");
        b.setOnAction(e -> onNew());

        box.getChildren().addAll(t1, t2, b);
        return box;
    }

    private Node dateHeader(final LocalDate date) {
        String title;
        final LocalDate today = LocalDate.now();

        if (date.equals(today)) title = t("history.today");
        else if (date.equals(today.minusDays(1))) title = t("history.yesterday");
        else title = date.toString();

        final Label lbl = new Label(title);
        lbl.getStyleClass().add("history-date");
        return lbl;
    }

    private Node journalCard(final JournalVM vm) {
        final VBox card = new VBox(8);
        card.getStyleClass().add("journal-card");

        final HBox top = new HBox(12);
        top.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        final VBox main = new VBox(4);

        final Label title = new Label(vm.title);
        title.getStyleClass().add("journal-card-title");

        final String time = " • " + TIME_FMT.format(vm.dateTime);
        final Label meta = new Label(t("journal.card.sub") + time);
        meta.getStyleClass().add("journal-card-meta");

        main.getChildren().addAll(title, meta);

        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        final HBox actions = new HBox(8);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        final Button btnEdit = new Button("✎");
        btnEdit.getStyleClass().addAll("icon-btn", "icon-btn-edit", "ms-icon");
        btnEdit.setOnAction(e -> {
            e.consume();
            openEdit(vm);
        });

        final Button btnDelete = new Button("\uD83D\uDDD1");
        btnDelete.getStyleClass().addAll("icon-btn", "icon-btn-delete", "ms-icon");
        btnDelete.setOnAction(e -> {
            e.consume();
            items.remove(vm);
            render();
        });

        actions.getChildren().addAll(btnEdit, btnDelete);

        top.getChildren().addAll(main, spacer, actions);

        final Label preview = new Label(makePreview(vm.content));
        preview.getStyleClass().add("journal-card-preview");
        preview.setWrapText(true);

        card.getChildren().addAll(top, preview);

        card.setOnMouseClicked(e -> openEdit(vm));

        return card;
    }

    // ======================
    // Overlay open/close
    // ======================

    private void showEditor() {
        overlay.setManaged(true);
        overlay.setVisible(true);

        editorHost.setManaged(true);
        editorHost.setVisible(true);

        editorHost.setTranslateX(430); // same as editor prefWidth
        final TranslateTransition tt = new TranslateTransition(Duration.millis(220), editorHost);
        tt.setToX(0);
        tt.play();
    }

    private void closeEditorInstant() {
        overlay.setVisible(false);
        overlay.setManaged(false);

        editorHost.setVisible(false);
        editorHost.setManaged(false);

        editorHost.setTranslateX(0);
    }

    private void closeEditor() {
        final TranslateTransition tt = new TranslateTransition(Duration.millis(160), editorHost);
        tt.setToX(430);
        tt.setOnFinished(e -> closeEditorInstant());
        tt.play();
    }

    // ======================
    // Guided serialization/parsing (Java 11)
    // ======================

    private static class Parsed {
        final String a1;
        final String a2;
        final String a3;

        Parsed(String a1, String a2, String a3) {
            this.a1 = a1;
            this.a2 = a2;
            this.a3 = a3;
        }
    }

    private String serializeGuided(final String a1, final String a2, final String a3) {
        return ""
                + "Q1[journal.prompt.context]\n"
                + "A1: " + safe(a1) + "\n\n"
                + "Q2[journal.prompt.inner]\n"
                + "A2: " + safe(a2) + "\n\n"
                + "Q3[journal.prompt.meaning]\n"
                + "A3: " + safe(a3) + "\n";
    }

    private Parsed parseGuided(final String content) {
        return new Parsed(
                extractAnswer(content, "A1:"),
                extractAnswer(content, "A2:"),
                extractAnswer(content, "A3:")
        );
    }

    private String extractAnswer(final String content, final String marker) {
        if (content == null) return "";
        final int i = content.indexOf(marker);
        if (i < 0) return "";
        final int start = i + marker.length();
        final int next = content.indexOf("\nQ", start);
        final String part = (next >= 0) ? content.substring(start, next) : content.substring(start);
        return part.trim();
    }

    private String makePreview(final String content) {
        final Parsed p = parseGuided(content);

        String v = firstNonEmpty(p.a1, p.a2, p.a3);
        if (v.isEmpty()) return t("journal.preview.empty");

        v = v.replace("\n", " ").trim();
        return v.length() > 120 ? v.substring(0, 120) + "…" : v;
    }

    // ======================
    // Temp seed
    // ======================

    private void seed() {
        if (!items.isEmpty()) return;

        items.add(new JournalVM(
                "First entry",
                "Q1[journal.prompt.context]\nA1: I want to write about something on my mind.\n\n"
                        + "Q2[journal.prompt.inner]\nA2: I feel a mix of pressure and excitement.\n\n"
                        + "Q3[journal.prompt.meaning]\nA3: I want to take it one step at a time.\n",
                LocalDateTime.now().minusHours(2)
        ));
    }

    // ======================
    // Helpers
    // ======================

    private String firstNonEmpty(final String a, final String b, final String c) {
        if (a != null && !a.trim().isEmpty()) return a.trim();
        if (b != null && !b.trim().isEmpty()) return b.trim();
        if (c != null && !c.trim().isEmpty()) return c.trim();
        return "";
    }

    private String safe(final String s) {
        return s == null ? "" : s.trim();
    }

    private String t(final String key) {
        try {
            return resources.getString(key);
        } catch (Exception e) {
            return key;
        }
    }
}
