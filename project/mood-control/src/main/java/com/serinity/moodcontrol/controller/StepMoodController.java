package com.serinity.moodcontrol.controller;

import javafx.animation.FillTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class StepMoodController {

    @FXML private Slider moodSlider;
    @FXML private Circle moodCircle;
    @FXML private Label moodLabel;

    private final DropShadow glow = new DropShadow();

    private static final Color C1 = Color.web("#6B7C93");
    private static final Color C2 = Color.web("#4F8FB8");
    private static final Color C3 = Color.web("#7A8C8C");
    private static final Color C4 = Color.web("#62B48F");
    private static final Color C5 = Color.web("#B9C56A");

    private static final String[] LABELS = {
            "Very Low", "Low", "Neutral", "Good", "Very Good"
    };

    @FXML
    public void initialize() {
        glow.setOffsetX(0);
        glow.setOffsetY(8);
        glow.setSpread(0.10);
        moodCircle.setEffect(glow);

        applyMood(3, false);

        moodSlider.valueProperty().addListener((obs, oldV, newV) -> {
            int level = (int) Math.round(newV.doubleValue());
            applyMood(level, true);
        });
    }

    private void applyMood(int level, boolean animate) {
        level = clamp(level, 1, 5);
        moodLabel.setText(LABELS[level - 1]);

        Color target = colorFor(level);
        double targetRadius = glowRadiusFor(level);
        Color glowCol = glowColorFor(level, target);

        if (!animate) {
            moodCircle.setFill(target);
            glow.setColor(glowCol);
            glow.setRadius(targetRadius);
            return;
        }

        FillTransition ft = new FillTransition(Duration.millis(220), moodCircle);
        ft.setToValue(target);
        ft.play();

        glow.setColor(glowCol);

        Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(220),
                        new KeyValue(glow.radiusProperty(), targetRadius)
                )
        );
        tl.play();
    }

    private Color colorFor(int level) {
        switch (level) {
            case 1: return C1;
            case 2: return C2;
            case 3: return C3;
            case 4: return C4;
            case 5: return C5;
            default: return C3;
        }
    }

    private double glowRadiusFor(int level) {
        switch (level) {
            case 1: return 10;
            case 2: return 14;
            case 3: return 18;
            case 4: return 22;
            case 5: return 26;
            default: return 18;
        }
    }

    private Color glowColorFor(int level, Color base) {
        double a;
        switch (level) {
            case 1: a = 0.18; break;
            case 2: a = 0.22; break;
            case 3: a = 0.26; break;
            case 4: a = 0.30; break;
            case 5: a = 0.34; break;
            default: a = 0.26;
        }
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), a);
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    public int getMoodLevel() {
        return (int) Math.round(moodSlider.getValue());
    }

    // ✅ PREFILL (for Edit)
    public void setMoodLevel(int level) {
        level = clamp(level, 1, 5);
        moodSlider.setValue(level);
        applyMood(level, false); // no animation on prefill
    }
}
