package com.serinity.moodcontrol.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;

import java.util.*;

public class StepEmotionsController {

    @FXML private FlowPane chipPane;
    @FXML private Label limitLabel;

    @FXML private ResourceBundle resources;

    private StateOfMindWizardController wizard;
    private boolean prefilling = false;

    private static final int MAX_SELECTED = 5;

    // Store CODES, not displayed text
    private final Set<String> selected = new LinkedHashSet<>();
    private final List<ToggleButton> buttons = new ArrayList<>();

    // Stable internal codes (DB-safe)
    private static final String[] EMOTION_CODES = {
            "CALM", "CONTENT", "HAPPY", "EXCITED", "GRATEFUL",
            "HOPEFUL", "CONFIDENT", "PROUD", "INSPIRED", "MOTIVATED",
            "NEUTRAL", "TIRED", "BORED", "NUMB", "OVERWHELMED",
            "ANXIOUS", "STRESSED", "WORRIED", "AFRAID", "INSECURE",
            "SAD", "LONELY", "DISAPPOINTED", "FRUSTRATED", "ANGRY",
            "GUILTY", "ASHAMED", "JEALOUS", "IRRITATED", "HURT"
    };

    public void setWizard(StateOfMindWizardController wizard) {
        this.wizard = wizard;
        updateNextState();
    }

    @FXML
    public void initialize() {
        if (resources == null) {
            throw new IllegalStateException("ResourceBundle not injected for StepEmotions. Load StepEmotions.fxml with a bundle.");
        }

        for (String code : EMOTION_CODES) {
            ToggleButton t = new ToggleButton(labelFor(code));
            t.getStyleClass().add("chip-toggle");
            t.setFocusTraversable(false);

            // Keep code attached to the button (no reliance on text)
            t.setUserData(code);

            t.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (prefilling) return;

                String emotionCode = (String) t.getUserData();
                if (emotionCode == null) emotionCode = "";

                if (isSelected) {
                    if (selected.size() >= MAX_SELECTED) {
                        t.setSelected(false);
                        return;
                    }
                    if (!emotionCode.isEmpty()) selected.add(emotionCode);
                } else {
                    selected.remove(emotionCode);
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
        // emotion.CALM etc. Fallback to code if missing
        String key = "emotion." + code;
        try {
            return resources.getString(key);
        } catch (MissingResourceException e) {
            return code; // dev-safe fallback
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
            // "Max selected ({0}). Remove one to choose another."
            String pattern = resources.getString("emotions.limit.max");
            limitLabel.setText(java.text.MessageFormat.format(pattern, MAX_SELECTED));
        } else {
            // "1 selection left." / "2 selections left."
            String one = resources.getString("emotions.limit.left.one");
            String many = resources.getString("emotions.limit.left.many");
            limitLabel.setText(java.text.MessageFormat.format(left == 1 ? one : many, left));
        }
    }

    // Returns CODES (DB-safe)
    public List<String> getSelectedEmotions() {
        return new ArrayList<>(selected);
    }

    // PREFILL for Edit: expects codes from DB
    public void setSelectedEmotions(List<String> codes) {
        prefilling = true;
        try {
            selected.clear();

            // clear UI first
            for (ToggleButton b : buttons) {
                b.setSelected(false);
            }

            if (codes != null) {
                for (String c : codes) {
                    if (c == null) continue;
                    String cleaned = c.trim().toUpperCase(Locale.ROOT);
                    if (!cleaned.isEmpty()) selected.add(cleaned);
                }
            }

            // apply selection to buttons based on code, not text
            for (ToggleButton b : buttons) {
                String code = (String) b.getUserData();
                if (code == null) continue;

                boolean match = false;
                for (String s : selected) {
                    if (s.equalsIgnoreCase(code)) {
                        match = true;
                        break;
                    }
                }
                if (match) b.setSelected(true);
            }

            // sync selected back from UI state
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
