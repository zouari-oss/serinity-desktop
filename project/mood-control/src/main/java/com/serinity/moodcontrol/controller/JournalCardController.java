package com.serinity.moodcontrol.controller;

import com.serinity.moodcontrol.model.JournalEntry;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class JournalCardController {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private VBox root;
    @FXML private Label titleLabel;
    @FXML private Label metaLabel;
    @FXML private Label previewLabel;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private JournalEntry entry;

    public void setData(
            final JournalEntry entry,
            final LocalDateTime dt,
            final ResourceBundle resources,
            final Consumer<JournalEntry> onEdit,
            final Consumer<JournalEntry> onDelete
    ) {
        this.entry = entry;

        final String title = (entry.getTitle() == null || entry.getTitle().trim().isEmpty())
                ? t(resources, "journal.untitled")
                : entry.getTitle().trim();

        titleLabel.setText(title);

        final String time = (dt == null) ? "" : (" • " + TIME_FMT.format(dt));
        metaLabel.setText(t(resources, "journal.card.sub") + time);

        previewLabel.setText(makePreview(resources, entry.getContent()));

        // Whole card click = edit
        root.setOnMouseClicked(e -> {
            if (onEdit != null) onEdit.accept(entry);
        });

        btnEdit.setOnAction(e -> {
            e.consume();
            if (onEdit != null) onEdit.accept(entry);
        });

        btnDelete.setOnAction(e -> {
            e.consume();
            if (onDelete != null) onDelete.accept(entry);
        });
    }

    // preview
    private static class Parsed {
        final String a1, a2, a3;
        Parsed(String a1, String a2, String a3) { this.a1 = a1; this.a2 = a2; this.a3 = a3; }
    }

    private String makePreview(final ResourceBundle resources, final String content) {
        final Parsed p = parseGuided(content);

        String v = firstNonEmpty(p.a1, p.a2, p.a3);
        if (v.isEmpty()) return t(resources, "journal.preview.empty");

        v = v.replace("\n", " ").trim();
        return v.length() > 120 ? v.substring(0, 120) + "…" : v;
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

    private String firstNonEmpty(final String a, final String b, final String c) {
        if (a != null && !a.trim().isEmpty()) return a.trim();
        if (b != null && !b.trim().isEmpty()) return b.trim();
        if (c != null && !c.trim().isEmpty()) return c.trim();
        return "";
    }

    private String t(final ResourceBundle rb, final String key) {
        try { return rb.getString(key); }
        catch (Exception e) { return key; }
    }
}
