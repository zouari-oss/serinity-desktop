package com.serinity.accesscontrol.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.ResourceBundle;
import java.util.function.Consumer;

public class JournalEditorController {

  public static class JournalDraft {
    public final String title;
    public final String a1, a2, a3;

    public JournalDraft(String title, String a1, String a2, String a3) {
      this.title = title;
      this.a1 = a1;
      this.a2 = a2;
      this.a3 = a3;
    }
  }

  private enum Mode {
    NEW, EDIT
  }

  // ----- FXML -----
  @FXML
  private Label editorTitle;
  @FXML
  private Label editorSubtitle;

  @FXML
  private TextField titleField;
  @FXML
  private TextArea a1Area;
  @FXML
  private TextArea a2Area;
  @FXML
  private TextArea a3Area;

  @FXML
  private Button btnSave;
  @FXML
  private Label saveHint;

  @FXML
  private ResourceBundle resources;

  // callbacks
  private Consumer<JournalDraft> onSave;
  private Runnable onCancel;

  // state
  private Mode mode = Mode.NEW;

  // snapshot for edit mode
  private String originalTitle = "";
  private String originalA1 = "";
  private String originalA2 = "";
  private String originalA3 = "";

  @FXML
  public void initialize() {
    // listen to changes and re-evaluate save state
    titleField.textProperty().addListener((obs, o, n) -> updateSaveState());
    a1Area.textProperty().addListener((obs, o, n) -> updateSaveState());
    a2Area.textProperty().addListener((obs, o, n) -> updateSaveState());
    a3Area.textProperty().addListener((obs, o, n) -> updateSaveState());

    // default disabled until opened
    if (btnSave != null)
      btnSave.setDisable(true);

    // hide hint by default
    if (saveHint != null) {
      saveHint.setVisible(false);
      saveHint.setManaged(false);
    }
  }

  // ======================
  // Public API
  // ======================

  public void openNew(String titleText, String subtitleText) {
    mode = Mode.NEW;

    editorTitle.setText(titleText);
    editorSubtitle.setText(subtitleText);

    titleField.clear();
    a1Area.clear();
    a2Area.clear();
    a3Area.clear();

    // reset anyway
    originalTitle = "";
    originalA1 = "";
    originalA2 = "";
    originalA3 = "";

    updateSaveState();
    titleField.requestFocus();
  }

  public void openEdit(String titleText, String subtitleText,
      String title, String a1, String a2, String a3) {

    mode = Mode.EDIT;

    editorTitle.setText(titleText);
    editorSubtitle.setText(subtitleText);

    titleField.setText(nullToEmpty(title));
    a1Area.setText(nullToEmpty(a1));
    a2Area.setText(nullToEmpty(a2));
    a3Area.setText(nullToEmpty(a3));

    // store snapshot for dirty checking
    originalTitle = safe(titleField.getText());
    originalA1 = safe(a1Area.getText());
    originalA2 = safe(a2Area.getText());
    originalA3 = safe(a3Area.getText());

    updateSaveState();
  }

  public void setOnSave(Consumer<JournalDraft> onSave) {
    this.onSave = onSave;
  }

  public void setOnCancel(Runnable onCancel) {
    this.onCancel = onCancel;
  }

  // ======================
  // Actions
  // ======================

  @FXML
  private void onSave() {
    if (btnSave != null && btnSave.isDisable())
      return; // safety

    if (onSave != null) {
      onSave.accept(new JournalDraft(
          safe(titleField.getText()),
          safe(a1Area.getText()),
          safe(a2Area.getText()),
          safe(a3Area.getText())));
    }
  }

  @FXML
  private void onCancel() {
    if (onCancel != null)
      onCancel.run();
  }

  // ======================
  // Save enabling logic
  // ======================

  private void updateSaveState() {
    if (btnSave == null)
      return;

    boolean enable = false;
    String hintKey = null;

    if (mode == Mode.NEW) {
      enable = !safe(titleField.getText()).isEmpty()
          && !safe(a1Area.getText()).isEmpty()
          && !safe(a2Area.getText()).isEmpty()
          && !safe(a3Area.getText()).isEmpty();

      if (!enable)
        hintKey = "journal.savehint.new";

    } else {
      String t = safe(titleField.getText());
      String a1 = safe(a1Area.getText());
      String a2 = safe(a2Area.getText());
      String a3 = safe(a3Area.getText());

      boolean changed = !t.equals(originalTitle)
          || !a1.equals(originalA1)
          || !a2.equals(originalA2)
          || !a3.equals(originalA3);

      enable = changed;

      if (!enable)
        hintKey = "journal.savehint.edit";
    }

    btnSave.setDisable(!enable);

    if (saveHint != null) {
      boolean show = !enable;

      if (show && hintKey != null) {
        saveHint.setText(t(hintKey));
      } else {
        saveHint.setText("");
      }

      saveHint.setVisible(show);
      saveHint.setManaged(show);
    }
  }

  // ======================
  // Helpers
  // ======================

  private String safe(String s) {
    return s == null ? "" : s.trim();
  }

  private String nullToEmpty(String s) {
    return s == null ? "" : s;
  }

  private String t(String key) {
    try {
      return resources.getString(key);
    } catch (Exception e) {
      return key;
    }
  }
}
