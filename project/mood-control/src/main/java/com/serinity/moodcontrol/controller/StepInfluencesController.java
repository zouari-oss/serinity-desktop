package com.serinity.moodcontrol.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;

import java.text.MessageFormat;
import java.util.*;

public class StepInfluencesController {

    @FXML private FlowPane chipPane;
    @FXML private Label limitLabel;

    @FXML private ResourceBundle resources;

    private StateOfMindWizardController wizard;
    private boolean prefilling = false;

    private static final int MAX_SELECTED = 5;

    // Store CODES, not display text
    private final Set<String> selected = new LinkedHashSet<>();
    private final List<ToggleButton> buttons = new ArrayList<>();

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

    public void setWizard(StateOfMindWizardController wizard) {
        this.wizard = wizard;
        updateNextState();
    }

    @FXML
    public void initialize() {
        if (resources == null) {
            throw new IllegalStateException("ResourceBundle not injected for StepInfluences. Load StepInfluences.fxml with a bundle.");
        }

        for (String code : INFLUENCE_CODES) {
            ToggleButton t = new ToggleButton(labelFor(code));
            t.getStyleClass().add("chip-toggle");
            t.setFocusTraversable(false);

            // Keep the code attached to the button
            t.setUserData(code);

            t.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (prefilling) return;

                String c = (String) t.getUserData();
                if (c == null) c = "";

                if (isSelected) {
                    if (selected.size() >= MAX_SELECTED) {
                        t.setSelected(false);
                        return;
                    }
                    if (!c.isEmpty()) selected.add(c);
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

    private String labelFor(String code) {
        String key = "influence." + code;
        try {
            return resources.getString(key);
        } catch (MissingResourceException e) {
            return code; // fallback
        }
    }

    private void updateNextState() {
        if (wizard != null) wizard.setCanGoNext(!selected.isEmpty());
    }

    private void updateDisableState() {
        boolean atMax = selected.size() >= MAX_SELECTED;
        for (ToggleButton b : buttons) {
            if (!b.isSelected()) b.setDisable(atMax);
            else b.setDisable(false);
        }
    }

    private void updateLimitUI() {
        int left = MAX_SELECTED - selected.size();

        if (left <= 0) {
            String pattern = resources.getString("influences.limit.max");
            limitLabel.setText(MessageFormat.format(pattern, MAX_SELECTED));
        } else {
            String one = resources.getString("influences.limit.left.one");
            String many = resources.getString("influences.limit.left.many");
            limitLabel.setText(MessageFormat.format(left == 1 ? one : many, left));
        }
    }

    // returns CODES (DB-safe)
    public List<String> getSelectedInfluences() {
        return new ArrayList<>(selected);
    }

    // PREFILL (for Edit) - expects CODES (what DAO should now return)
    public void setSelectedInfluences(List<String> codes) {
        prefilling = true;
        try {
            selected.clear();

            for (ToggleButton b : buttons) b.setSelected(false);

            if (codes != null) {
                for (String c : codes) {
                    if (c == null) continue;
                    String v = c.trim().toUpperCase(Locale.ROOT);
                    if (!v.isEmpty()) selected.add(v);
                }
            }

            // apply selection based on code
            for (ToggleButton b : buttons) {
                String code = (String) b.getUserData();
                if (code != null && selected.contains(code)) {
                    b.setSelected(true);
                }
            }

            // sync selected from UI
            selected.clear();
            for (ToggleButton b : buttons) {
                if (b.isSelected()) {
                    String code = (String) b.getUserData();
                    if (code != null && !code.trim().isEmpty()) selected.add(code);
                }
            }

        } finally {
            prefilling = false;
        }

        updateLimitUI();
        updateDisableState();
        updateNextState();
    }
}
