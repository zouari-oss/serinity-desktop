package com.serinity.moodcontrol.controller;

import com.serinity.moodcontrol.dao.JournalEntryDao;
import com.serinity.moodcontrol.model.JournalEntry;
import com.serinity.moodcontrol.service.JournalService;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ArrayList;


public class JournalController {

    // kima Journal.fxml prefWidth
    private static final double EDITOR_WIDTH = 430.0;

    // TEMP until users module
    private static final long USER_ID = 1L;

    @FXML private VBox journalBox;

    @FXML private StackPane overlay;
    @FXML private StackPane editorHost;

    @FXML private ResourceBundle resources;

    private JournalEditorController editor;

    // DB
    private final JournalEntryDao dao = new JournalEntryDao();

    private final JournalService journalService =
            new JournalService(dao, this::serializeGuided);


    // current data
    private List<JournalEntry> items = new ArrayList<JournalEntry>();

    // tracki current "edit context"
    private JournalEntry editing = null;

    @FXML
    public void initialize() {
        if (resources == null) {
            throw new IllegalStateException(
                    "ResourceBundle not injected in JournalController. Load Journal.fxml with a bundle."
            );
        }

        loadEditor();
        reloadFromDb();
        render();
        closeEditorInstant();
    }


    //  editor component

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

    //  actions


    @FXML
    private void onNew() {
        editing = null;

        editor.openNew(
                t("journal.editor.new"),
                t("journal.editor.subtitle")
        );

        showEditor();
    }

    @FXML
    private void onRefresh() {
        reloadFromDb();
        render();
    }

    // DB load


    private void reloadFromDb() {
        try {
            items = dao.findAll(USER_ID);
        } catch (SQLException e) {
            e.printStackTrace();
            items = new ArrayList<JournalEntry>();
        }
    }

    // Editor

    private void onEditorSave(final JournalEditorController.JournalDraft d) {
        String title = safe(d.title);

        String err = (editing == null)
                ? journalService.create(USER_ID, title, d.a1, d.a2, d.a3)
                : journalService.update(USER_ID, editing, title, d.a1, d.a2, d.a3);

        if (err != null) {
            // err format: "journal.field.title|journal.validation.empty"
            String[] parts = err.split("\\|", 2);
            String fieldKey = parts.length > 0 ? parts[0] : err;
            String ruleKey  = parts.length > 1 ? parts[1] : "journal.validation.invalid";

            final javafx.scene.control.Alert alert =
                    new javafx.scene.control.Alert(Alert.AlertType.WARNING);

            alert.setTitle(t("journal.validation.title"));
            alert.setHeaderText(null);
            alert.setContentText(t(fieldKey) + " " + t(ruleKey));

            alert.getButtonTypes().setAll(javafx.scene.control.ButtonType.OK);

            alert.getDialogPane().getStylesheets().add(
                    getClass().getResource("/styles/styles.css").toExternalForm()
            );
            alert.getDialogPane().getStyleClass().add("app-dialog");

            alert.showAndWait();
            return;
        }

        editing = null;
        reloadFromDb();
        render();
        closeEditor();
    }


    private void openEdit(final JournalEntry entry) {
        editing = entry;

        final Parsed p = parseGuided(entry.getContent());

        editor.openEdit(
                t("journal.editor.edit"),
                t("journal.editor.subtitle"),
                entry.getTitle(),
                p.a1, p.a2, p.a3
        );

        showEditor();
    }

    // Rendering

    private void render() {
        journalBox.getChildren().clear();

        if (items == null || items.isEmpty()) {
            journalBox.getChildren().add(emptyState());
            return;
        }

        LocalDate current = null;
        for (final JournalEntry e : items) {
            LocalDateTime dt = e.getCreatedAt();
            if (dt == null) dt = LocalDateTime.now();

            final LocalDate d = dt.toLocalDate();
            if (current == null || !current.equals(d)) {
                current = d;
                journalBox.getChildren().add(dateHeader(d));
            }

            journalBox.getChildren().add(journalCard(e, dt)); // <-- component now
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

    private Node journalCard(final JournalEntry entry, final LocalDateTime dt) {
        try {
            final FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/mood/components/JournalCard.fxml"),
                    resources
            );

            final Node card = loader.load();

            final JournalCardController c = loader.getController();
            c.setData(
                    entry,
                    dt,
                    resources,
                    e -> openEdit(e),
                    e -> onDelete(e)
            );

            return card;

        } catch (final Exception e) {
            throw new RuntimeException("Failed to load JournalCard.fxml", e);
        }
    }

    private void onDelete(final JournalEntry entry) {
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle(t("journal.delete.title"));
        alert.setHeaderText(t("journal.delete.header"));
        alert.setContentText(t("journal.delete.body"));

        final Optional<ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                dao.delete(entry.getId(), USER_ID);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            reloadFromDb();
            render();
        }
    }

    // Overlay open/close

    private void showEditor() {
        overlay.setManaged(true);
        overlay.setVisible(true);

        editorHost.setManaged(true);
        editorHost.setVisible(true);

        editorHost.setTranslateX(EDITOR_WIDTH);
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
        tt.setToX(EDITOR_WIDTH);
        tt.setOnFinished(e -> closeEditorInstant());
        tt.play();
    }


    // partie serialisation, el QnA lkol fi string


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

    // DB insert trim

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
