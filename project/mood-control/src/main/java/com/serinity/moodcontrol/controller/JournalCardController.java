package com.serinity.moodcontrol.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serinity.moodcontrol.model.JournalEntry;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class JournalCardController {

  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @FXML
  private VBox root;
  @FXML
  private Label titleLabel;
  @FXML
  private Label metaLabel;
  @FXML
  private Label previewLabel;
  @FXML
  private Button btnEdit;
  @FXML
  private Button btnDelete;

  @FXML
  private FlowPane aiTagsPane;

  private JournalEntry entry;

  public void setData(
      final JournalEntry entry,
      final LocalDateTime dt,
      final ResourceBundle resources,
      final Consumer<JournalEntry> onEdit,
      final Consumer<JournalEntry> onDelete) {
    this.entry = entry;

    final String title = (entry.getTitle() == null || entry.getTitle().trim().isEmpty())
        ? t(resources, "journal.untitled")
        : entry.getTitle().trim();

    titleLabel.setText(title);

    final String time = (dt == null) ? "" : (" • " + TIME_FMT.format(dt));
    metaLabel.setText(t(resources, "journal.card.sub") + time);

    previewLabel.setText(makePreview(resources, entry.getContent()));

    renderAiTags(entry.getAiTags());

    // Whole card click = edit
    root.setOnMouseClicked(e -> {
      if (onEdit != null)
        onEdit.accept(entry);
    });

    btnEdit.setOnAction(e -> {
      e.consume();
      if (onEdit != null)
        onEdit.accept(entry);
    });

    btnDelete.setOnAction(e -> {
      e.consume();
      if (onDelete != null)
        onDelete.accept(entry);
    });
  }

  private void renderAiTags(final String aiTagsJson) {
    if (aiTagsPane == null)
      return;

    aiTagsPane.getChildren().clear();

    if (aiTagsJson == null || aiTagsJson.trim().isEmpty() || "[]".equals(aiTagsJson.trim())) {
      aiTagsPane.setVisible(false);
      aiTagsPane.setManaged(false);
      return;
    }

    List<Map<String, Object>> arr = parseJsonArray(aiTagsJson);
    if (arr.isEmpty()) {
      aiTagsPane.setVisible(false);
      aiTagsPane.setManaged(false);
      return;
    }

    aiTagsPane.setVisible(true);
    aiTagsPane.setManaged(true);

    for (Map<String, Object> obj : arr) {
      Object tagObj = obj.get("tag");
      if (tagObj == null)
        tagObj = obj.get("label");
      if (tagObj == null)
        continue;

      String tag = String.valueOf(tagObj).trim().toLowerCase();
      if (tag.isEmpty())
        continue;

      Label chip = new Label(capitalize(tag));
      chip.getStyleClass().add("ai-chip");
      chip.getStyleClass().add("ai-chip-" + tag); // ai-chip-stress, ai-chip-anger, etc.

      aiTagsPane.getChildren().add(chip);
    }
  }

  private List<Map<String, Object>> parseJsonArray(final String json) {
    try {
      JsonNode root = MAPPER.readTree(json);
      return normalizeTags(root);
    } catch (Exception e) {
      // If JSON is malformed, just hide chips rather than crash UI
      return Collections.emptyList();
    }
  }

  private List<Map<String, Object>> normalizeTags(final JsonNode root) {
    if (root == null || root.isNull())
      return Collections.emptyList();

    // Handle double-encoded JSON: "\"{...}\"" or "\"[...]\""
    if (root.isTextual()) {
      final String nested = root.asText();
      if (nested != null && !nested.trim().isEmpty()) {
        try {
          return normalizeTags(MAPPER.readTree(nested));
        } catch (Exception ignored) {
          return Collections.emptyList();
        }
      }
      return Collections.emptyList();
    }

    // Native Java format: [{"tag":"fear","score":0.28571}, ...]
    // Also accept direct web label array: [{"label":"neutral","score":0.99}, ...]
    if (root.isArray()) {
      return normalizeLabelArray(root);
    }

    // Web format object:
    // {"top_label":"neutral","labels":[{"label":"neutral","score":0.99}], ...}
    if (root.isObject()) {
      JsonNode labels = root.get("labels");
      if (labels != null) {
        if (labels.isArray())
          return normalizeLabelArray(labels);
        if (labels.isTextual()) {
          try {
            return normalizeTags(MAPPER.readTree(labels.asText()));
          } catch (Exception ignored) {
            // Fall through to top_label fallback
          }
        }
      }

      JsonNode topLabel = root.get("top_label");
      if (topLabel != null && !topLabel.isNull() && !topLabel.asText("").trim().isEmpty()) {
        Map<String, Object> mapped = new LinkedHashMap<>();
        mapped.put("tag", topLabel.asText());
        return List.of(mapped);
      }
    }

    return Collections.emptyList();
  }

  private List<Map<String, Object>> normalizeLabelArray(final JsonNode arr) {
    List<Map<String, Object>> out = new ArrayList<>();
    for (JsonNode item : arr) {
      if (item == null || item.isNull())
        continue;

      JsonNode labelNode = item.get("tag");
      if (labelNode == null || labelNode.isNull())
        labelNode = item.get("label");

      if (labelNode == null || labelNode.isNull() || labelNode.asText("").trim().isEmpty())
        continue;

      Map<String, Object> mapped = new LinkedHashMap<>();
      mapped.put("tag", labelNode.asText());

      JsonNode score = item.get("score");
      if (score != null && score.isNumber())
        mapped.put("score", score.numberValue());

      out.add(mapped);
    }
    return out;
  }

  private String capitalize(String s) {
    if (s == null || s.isEmpty())
      return s;
    return s.substring(0, 1).toUpperCase() + s.substring(1);
  }

  // preview helpers
  private static class Parsed {
    final String a1, a2, a3;

    Parsed(String a1, String a2, String a3) {
      this.a1 = a1;
      this.a2 = a2;
      this.a3 = a3;
    }
  }

  private String makePreview(final ResourceBundle resources, final String content) {
    final Parsed p = parseGuided(content);

    String v = firstNonEmpty(p.a1, p.a2, p.a3);
    if (v.isEmpty())
      return t(resources, "journal.preview.empty");

    v = v.replace("\n", " ").trim();
    return v.length() > 120 ? v.substring(0, 120) + "…" : v;
  }

  private Parsed parseGuided(final String content) {
    return new Parsed(
        extractAnswer(content, "A1:"),
        extractAnswer(content, "A2:"),
        extractAnswer(content, "A3:"));
  }

  private String extractAnswer(final String content, final String marker) {
    if (content == null)
      return "";
    final int i = content.indexOf(marker);
    if (i < 0)
      return "";
    final int start = i + marker.length();
    final int next = content.indexOf("\nQ", start);
    final String part = (next >= 0) ? content.substring(start, next) : content.substring(start);
    return part.trim();
  }

  private String firstNonEmpty(final String a, final String b, final String c) {
    if (a != null && !a.trim().isEmpty())
      return a.trim();
    if (b != null && !b.trim().isEmpty())
      return b.trim();
    if (c != null && !c.trim().isEmpty())
      return c.trim();
    return "";
  }

  private String t(final ResourceBundle rb, final String key) {
    try {
      return rb.getString(key);
    } catch (Exception e) {
      return key;
    }
  }
}
