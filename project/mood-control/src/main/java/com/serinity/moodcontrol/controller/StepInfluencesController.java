package com.serinity.moodcontrol.controller;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;

public class StepInfluencesController {

  private static final int MAX_SELECTED = 5;
  // Codes derived from your DB "influence.name"
  // Sleep -> SLEEP
  // School/Work -> SCHOOL_WORK
  // Social media -> SOCIAL_MEDIA
  // Travel/Commute -> TRAVEL_COMMUTE
  private static final String[] INFLUENCE_CODES = {
      "SLEEP", "SCHOOL_WORK", "DEADLINES", "STRESS",
      "FRIENDS", "FAMILY", "RELATIONSHIP", "SOCIAL_MEDIA",
      "HEALTH", "PAIN", "EXERCISE", "FOOD",
      "WEATHER", "NEWS", "MONEY", "TRAVEL_COMMUTE",
      "LONELINESS", "CONFLICT", "ACHIEVEMENT", "FAILURE",
      "RELAXATION", "MUSIC", "GAMING", "STUDY",
      "THERAPY", "MEDICATION", "CAFFEINE"
  };

  @FXML
  private FlowPane chipPane;

  @FXML
  private Label limitLabel;
  @FXML
  private ResourceBundle resources;

  private StateOfMindWizardController wizard;

  private boolean prefilling = false;
  // Store CODES, not display text
  private final Set<String> selected = new LinkedHashSet<>();

  private final List<ToggleButton> buttons = new ArrayList<>();

  public void setWizard(final StateOfMindWizardController wizard) {
    this.wizard = wizard;
    updateNextState();
  }

  @FXML
  public void initialize() {
    if (resources == null) {
      throw new IllegalStateException(
          "ResourceBundle not injected for StepInfluences. Load StepInfluences.fxml with a bundle.");
    }

    for (final String code : INFLUENCE_CODES) {
      final ToggleButton t = new ToggleButton(labelFor(code));
      t.getStyleClass().add("chip-toggle");
      t.setFocusTraversable(false);

      // Keep the code attached to the button
      t.setUserData(code);

      t.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
        if (prefilling)
          return;

        String c = (String) t.getUserData();
        if (c == null)
          c = "";

        if (isSelected) {
          if (selected.size() >= MAX_SELECTED) {
            t.setSelected(false);
            return;
          }
          if (!c.isEmpty())
            selected.add(c);
        } else {
          selected.remove(c);
        }

        updateLimitUI();
        updateDisableState();
        updateNextState();
      });

      buttons.add(t);
      chipPane.getChildren().add(t);
    }

    updateLimitUI();
    updateDisableState();
    updateNextState();
  }

  // returns CODES (DB-safe)
  public List<String> getSelectedInfluences() {
    return new ArrayList<>(selected);
  }

  // PREFILL (for Edit) - expects CODES (what DAO should now return)
  public void setSelectedInfluences(final List<String> codes) {
    prefilling = true;
    try {
      selected.clear();

      for (final ToggleButton b : buttons)
        b.setSelected(false);

      if (codes != null) {
        for (final String c : codes) {
          if (c == null)
            continue;
          final String v = c.trim().toUpperCase(Locale.ROOT);
          if (!v.isEmpty())
            selected.add(v);
        }
      }

      // apply selection based on code
      for (final ToggleButton b : buttons) {
        final String code = (String) b.getUserData();
        if (code != null && selected.contains(code)) {
          b.setSelected(true);
        }
      }

      // sync selected from UI
      selected.clear();
      for (final ToggleButton b : buttons) {
        if (b.isSelected()) {
          final String code = (String) b.getUserData();
          if (code != null && !code.trim().isEmpty())
            selected.add(code);
        }
      }

    } finally {
      prefilling = false;
    }

    updateLimitUI();
    updateDisableState();
    updateNextState();
  }

  private String labelFor(final String code) {
    final String key = "influence." + code;
    try {
      return resources.getString(key);
    } catch (final MissingResourceException e) {
      return code; // fallback
    }
  }

  private void updateNextState() {
    if (wizard != null)
      wizard.setCanGoNext(!selected.isEmpty());
  }

  private void updateDisableState() {
    final boolean atMax = selected.size() >= MAX_SELECTED;
    for (final ToggleButton b : buttons) {
      if (!b.isSelected())
        b.setDisable(atMax);
      else
        b.setDisable(false);
    }
  }

  private void updateLimitUI() {
    final int left = MAX_SELECTED - selected.size();

    if (left <= 0) {
      final String pattern = resources.getString("influences.limit.max");
      limitLabel.setText(MessageFormat.format(pattern, MAX_SELECTED));
    } else {
      final String one = resources.getString("influences.limit.left.one");
      final String many = resources.getString("influences.limit.left.many");
      limitLabel.setText(MessageFormat.format(left == 1 ? one : many, left));
    }
  }
} // StepInfluencesController class
