package com.serinity.moodcontrol.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;

import java.util.*;

public class StepEmotionsController {

    @FXML private FlowPane chipPane;
    @FXML private Label limitLabel;

    private StateOfMindWizardController wizard;
    private boolean prefilling = false; // prefiller boolean

    private static final int MAX_SELECTED = 5;

    private final Set<String> selected = new LinkedHashSet<>();
    private final List<ToggleButton> buttons = new ArrayList<>();

    private static final String[] EMOTIONS = {
            "Calm", "Content", "Happy", "Excited", "Grateful",
            "Hopeful", "Confident", "Proud", "Inspired", "Motivated",
            "Neutral", "Tired", "Bored", "Numb", "Overwhelmed",
            "Anxious", "Stressed", "Worried", "Afraid", "Insecure",
            "Sad", "Lonely", "Disappointed", "Frustrated", "Angry",
            "Guilty", "Ashamed", "Jealous", "Irritated", "Hurt"
    };

    public void setWizard(StateOfMindWizardController wizard) {
        this.wizard = wizard;
        updateNextState();
    }

    @FXML
    public void initialize() {
        for (String emotion : EMOTIONS) {
            ToggleButton t = new ToggleButton(emotion);
            t.getStyleClass().add("chip-toggle");
            t.setFocusTraversable(false);

            t.selectedProperty().addListener((obs, wasSelected, isSelected) -> {

                // ignore events triggered by prefill
                if (prefilling) return;

                String key = t.getText() == null ? "" : t.getText().trim();

                if (isSelected) {
                    if (selected.size() >= MAX_SELECTED) {
                        t.setSelected(false);
                        return;
                    }
                    if (!key.isEmpty()) selected.add(key);
                } else {
                    selected.remove(key);
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
        if (left <= 0) limitLabel.setText("Max selected (" + MAX_SELECTED + "). Remove one to choose another.");
        else limitLabel.setText(left + " selection" + (left == 1 ? "" : "s") + " left.");
    }

    public List<String> getSelectedEmotions() {
        return new ArrayList<>(selected);
    }

    //  PREFILL for Edit
    public void setSelectedEmotions(List<String> names) {
        prefilling = true;
        try {
            selected.clear();

            // 1) clear buttons first
            for (ToggleButton b : buttons) {
                b.setSelected(false);
            }

            // 2) keep ALL saved names (no MAX_SELECTED break)
            if (names != null) {
                for (String n : names) {
                    if (n == null) continue;
                    String cleaned = n.trim();
                    if (!cleaned.isEmpty()) selected.add(cleaned);
                }
            }

            // 3) apply selection to buttons
            for (ToggleButton b : buttons) {
                String txt = (b.getText() == null) ? "" : b.getText().trim();

                boolean match = false;
                for (String s : selected) {
                    if (s != null && s.trim().equalsIgnoreCase(txt)) {
                        match = true;
                        break;
                    }
                }

                if (match) b.setSelected(true);
            }

            // 4) sync selected back from actual UI state (important)
            selected.clear();
            for (ToggleButton b : buttons) {
                if (b.isSelected()) {
                    String txt = (b.getText() == null) ? "" : b.getText().trim();
                    if (!txt.isEmpty()) selected.add(txt);
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
