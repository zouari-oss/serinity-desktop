package com.serinity.exercicecontrol.controller;

import com.serinity.exercicecontrol.model.BreathingPlan;
import com.serinity.exercicecontrol.model.BreathingProtocol;
import com.serinity.exercicecontrol.model.BreathingState;
import com.serinity.exercicecontrol.service.BreathingService;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.IOException;

public class BreathingController {

    @FXML private ComboBox<BreathingState> cbState;

    @FXML private Label lblProtocolName;
    @FXML private Label lblBenefit;
    @FXML private Label lblWhy;

    @FXML private Label lblStateBadge;
    @FXML private Label chipDuration;
    @FXML private Label chipCycle;
    @FXML private Label chipGoal;

    // Cycle preview
    @FXML private Label lblInhaleSec;
    @FXML private Label lblHold1Sec;
    @FXML private Label lblExhaleSec;
    @FXML private Label lblHold2Sec;

    @FXML private ProgressBar barInhale;
    @FXML private ProgressBar barHold1;
    @FXML private ProgressBar barExhale;
    @FXML private ProgressBar barHold2;

    // Cards/containers for palette
    @FXML private VBox protocolCard;
    @FXML private StackPane orbCard;
    @FXML private VBox controlCard;

    // Gradient behind orb
    @FXML private Region orbGradient;

    // Main UI
    @FXML private ProgressIndicator ringProgress;
    @FXML private ProgressBar progressBar;
    @FXML private Circle breathOrb;

    @FXML private Label lblPhaseBig;
    @FXML private Label lblPhaseSmall;
    @FXML private Label lblPercent;
    @FXML private Label lblTimeLeft;

    @FXML private Label lblHint;
    @FXML private Label lblCycleInfo;

    @FXML private Button btnStart;
    @FXML private Button btnStop;

    private final BreathingService breathingService = new BreathingService();

    private Timeline tickTimeline;
    private Animation currentOrbAnim;

    private BreathingPlan currentPlan;

    private int totalSec;
    private int elapsedSec;

    private int phaseRemaining;
    private Phase phase;

    private enum Phase { INHALE, HOLD1, EXHALE, HOLD2 }

    // Glow
    private final DropShadow glow = new DropShadow();

    @FXML
    public void initialize() {
        cbState.getItems().setAll(BreathingState.values());
        cbState.getSelectionModel().select(BreathingState.STRESS_AIGU);

        cbState.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(BreathingState s) { return s == null ? "" : stateLabel(s); }
            @Override public BreathingState fromString(String string) { return null; }
        });

        btnStop.setDisable(true);
        ringProgress.setProgress(0);
        progressBar.setProgress(0);

        breathOrb.setScaleX(0.88);
        breathOrb.setScaleY(0.88);

        glow.setRadius(0);
        glow.setSpread(0.18);
        glow.setOffsetX(0);
        glow.setOffsetY(0);
        breathOrb.setEffect(glow);

        cbState.setOnAction(e -> onStateChanged());
        onStateChanged();
    }

    private void onStateChanged() {
        currentPlan = breathingService.recommend(cbState.getValue());
        BreathingProtocol p = currentPlan.getProtocol();
        BreathingState st = cbState.getValue();

        lblProtocolName.setText(p.getName());
        lblBenefit.setText(p.getBenefit());
        lblWhy.setText(currentPlan.getWhy());

        lblStateBadge.setText(stateLabel(st));
        chipDuration.setText("⏱ " + formatSeconds(p.getTotalSeconds()));
        chipCycle.setText("🔁 " + p.getInhale() + "-" + p.getHold1() + "-" + p.getExhale() + "-" + p.getHold2());
        chipGoal.setText("🎯 " + goalLabel(st));

        lblInhaleSec.setText("Inspire " + p.getInhale() + "s");
        lblHold1Sec.setText("Garde " + p.getHold1() + "s");
        lblExhaleSec.setText("Expire " + p.getExhale() + "s");
        lblHold2Sec.setText("Pause " + p.getHold2() + "s");

        updateCycleBars(p);
        applyPalette(st);

        lblCycleInfo.setText("Cycle: " + p.getInhale() + "-" + p.getHold1() + "-" + p.getExhale() + "-" + p.getHold2() + "s");
        lblHint.setText("Suis le cercle : il gonfle à l’inspiration et descend à l’expiration.");

        resetUI();
    }

    @FXML
    private void onStart() {
        if (currentPlan == null) return;

        stopAll();

        BreathingProtocol p = currentPlan.getProtocol();
        totalSec = p.getTotalSeconds();
        elapsedSec = 0;

        phase = Phase.INHALE;
        phaseRemaining = Math.max(1, p.getInhale());
        setPhaseLabels();

        btnStart.setDisable(true);
        btnStop.setDisable(false);

        ringProgress.setProgress(0);
        progressBar.setProgress(0);
        lblPercent.setText("0%");
        lblTimeLeft.setText(formatSeconds(totalSec));

        animateOrbForPhase(p);
        animateGlowForPhase();

        tickTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        tickTimeline.setCycleCount(Timeline.INDEFINITE);
        tickTimeline.play();
    }

    @FXML
    private void onStop() {
        stopAll();
        resetUI();
    }

    @FXML
    private void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exercice/ExerciseList.fxml"));
            Parent root = loader.load();
            StackPane host = (StackPane) lblPhaseBig.getScene().lookup("#contentHost");
            if (host != null) host.getChildren().setAll(root);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Retour impossible.").showAndWait();
        }
    }

    private void tick() {
        if (currentPlan == null) return;
        BreathingProtocol p = currentPlan.getProtocol();

        elapsedSec++;
        int left = Math.max(0, totalSec - elapsedSec);

        double prog = Math.min(1.0, elapsedSec / (double) totalSec);
        ringProgress.setProgress(prog);
        progressBar.setProgress(prog);
        lblPercent.setText((int) Math.round(prog * 100) + "%");
        lblTimeLeft.setText(formatSeconds(left));

        phaseRemaining--;
        if (phaseRemaining <= 0) {
            nextPhase(p);
            setPhaseLabels();
            animateOrbForPhase(p);
            animateGlowForPhase();
        }

        if (elapsedSec >= totalSec) {
            stopAll();
            btnStart.setDisable(false);
            btnStop.setDisable(true);

            lblPhaseBig.setText("✅ Terminé");
            lblPhaseSmall.setText("Bien joué.");
            lblHint.setText("Séance terminée. Tu peux relancer ou changer d’état.");

            glow.setRadius(0);
        }
    }

    private void nextPhase(BreathingProtocol p) {
        switch (phase) {
            case INHALE -> {
                if (p.getHold1() > 0) { phase = Phase.HOLD1; phaseRemaining = p.getHold1(); }
                else { phase = Phase.EXHALE; phaseRemaining = Math.max(1, p.getExhale()); }
            }
            case HOLD1 -> { phase = Phase.EXHALE; phaseRemaining = Math.max(1, p.getExhale()); }
            case EXHALE -> {
                if (p.getHold2() > 0) { phase = Phase.HOLD2; phaseRemaining = p.getHold2(); }
                else { phase = Phase.INHALE; phaseRemaining = Math.max(1, p.getInhale()); }
            }
            case HOLD2 -> { phase = Phase.INHALE; phaseRemaining = Math.max(1, p.getInhale()); }
        }
    }

    private void setPhaseLabels() {
        switch (phase) {
            case INHALE -> { lblPhaseBig.setText("INSPIRE"); lblPhaseSmall.setText("Remplis doucement"); }
            case HOLD1 -> { lblPhaseBig.setText("GARDE"); lblPhaseSmall.setText("Stabilise"); }
            case EXHALE -> { lblPhaseBig.setText("EXPIRE"); lblPhaseSmall.setText("Relâche"); }
            case HOLD2 -> { lblPhaseBig.setText("PAUSE"); lblPhaseSmall.setText("Respire tranquille"); }
        }
    }

    // ----- WOW: palette + gradient + glow -----

    private void applyPalette(BreathingState st) {
        removeStateClasses(protocolCard);
        removeStateClasses(orbCard);
        removeStateClasses(controlCard);

        removeBadgeClasses(lblStateBadge);
        removeChipClasses(chipDuration);
        removeChipClasses(chipCycle);
        removeChipClasses(chipGoal);

        String stateClass = switch (st) {
            case STRESS_AIGU -> "state-stress";
            case FATIGUE -> "state-fatigue";
            case RUMINATIONS -> "state-ruminations";
            case NORMAL -> "state-normal";
        };

        protocolCard.getStyleClass().add(stateClass);
        orbCard.getStyleClass().add(stateClass);
        controlCard.getStyleClass().add(stateClass);

        lblStateBadge.getStyleClass().add(switch (st) {
            case STRESS_AIGU -> "badge-stress";
            case FATIGUE -> "badge-fatigue";
            case RUMINATIONS -> "badge-ruminations";
            case NORMAL -> "badge-normal";
        });

        String chipClass = switch (st) {
            case STRESS_AIGU -> "chip-stress";
            case FATIGUE -> "chip-fatigue";
            case RUMINATIONS -> "chip-ruminations";
            case NORMAL -> "chip-normal";
        };
        chipDuration.getStyleClass().add(chipClass);
        chipCycle.getStyleClass().add(chipClass);
        chipGoal.getStyleClass().add(chipClass);

        String fill, stroke, gradient;
        Color glowColor;

        switch (st) {
            case STRESS_AIGU -> {
                fill = "rgba(60,120,240,0.30)";
                stroke = "rgba(60,120,240,0.95)";
                gradient = "linear-gradient(to bottom right, rgba(60,120,240,0.22), rgba(0,0,0,0.02))";
                glowColor = Color.rgb(60, 120, 240, 0.85);
            }
            case FATIGUE -> {
                fill = "rgba(60,180,90,0.26)";
                stroke = "rgba(60,180,90,0.95)";
                gradient = "linear-gradient(to bottom right, rgba(60,180,90,0.20), rgba(0,0,0,0.02))";
                glowColor = Color.rgb(60, 180, 90, 0.85);
            }
            case RUMINATIONS -> {
                fill = "rgba(150,90,220,0.26)";
                stroke = "rgba(150,90,220,0.95)";
                gradient = "linear-gradient(to bottom right, rgba(150,90,220,0.20), rgba(0,0,0,0.02))";
                glowColor = Color.rgb(150, 90, 220, 0.85);
            }
            default -> {
                fill = "rgba(30,140,160,0.28)";
                stroke = "rgba(30,140,160,0.95)";
                gradient = "linear-gradient(to bottom right, rgba(30,140,160,0.20), rgba(0,0,0,0.02))";
                glowColor = Color.rgb(30, 140, 160, 0.85);
            }
        }

        breathOrb.setStyle("-fx-fill: " + fill + "; -fx-stroke: " + stroke + "; -fx-stroke-width: 4;");
        orbGradient.setStyle("-fx-background-radius: 18; -fx-background-color: " + gradient + ";");
        glow.setColor(glowColor);
    }

    private void animateGlowForPhase() {
        double targetRadius, targetSpread;
        switch (phase) {
            case INHALE -> { targetRadius = 30; targetSpread = 0.25; }
            case EXHALE -> { targetRadius = 18; targetSpread = 0.18; }
            case HOLD1, HOLD2 -> { targetRadius = 12; targetSpread = 0.14; }
            default -> { targetRadius = 10; targetSpread = 0.12; }
        }

        Timeline tt = new Timeline(
                new KeyFrame(Duration.millis(0),
                        new KeyValue(glow.radiusProperty(), glow.getRadius()),
                        new KeyValue(glow.spreadProperty(), glow.getSpread())),
                new KeyFrame(Duration.millis(280),
                        new KeyValue(glow.radiusProperty(), targetRadius, Interpolator.EASE_BOTH),
                        new KeyValue(glow.spreadProperty(), targetSpread, Interpolator.EASE_BOTH))
        );
        tt.play();
    }

    private void updateCycleBars(BreathingProtocol p) {
        int max = Math.max(1, Math.max(Math.max(p.getInhale(), p.getHold1()), Math.max(p.getExhale(), p.getHold2())));
        barInhale.setProgress(p.getInhale() / (double) max);
        barHold1.setProgress(p.getHold1() / (double) max);
        barExhale.setProgress(p.getExhale() / (double) max);
        barHold2.setProgress(p.getHold2() / (double) max);

        if (p.getHold1() == 0) barHold1.setProgress(0.04);
        if (p.getHold2() == 0) barHold2.setProgress(0.04);
    }

    private void animateOrbForPhase(BreathingProtocol p) {
        int sec = Math.max(1, currentPhaseDuration(p));
        if (currentOrbAnim != null) currentOrbAnim.stop();

        double from = breathOrb.getScaleX();
        double to;

        switch (phase) {
            case INHALE -> to = 1.10;
            case EXHALE -> to = 0.82;
            case HOLD1, HOLD2 -> to = from;
            default -> to = from;
        }

        if (phase == Phase.HOLD1 || phase == Phase.HOLD2) {
            Timeline pulse = new Timeline(
                    new KeyFrame(Duration.seconds(0),
                            new KeyValue(breathOrb.scaleXProperty(), from),
                            new KeyValue(breathOrb.scaleYProperty(), from)),
                    new KeyFrame(Duration.seconds(sec / 2.0),
                            new KeyValue(breathOrb.scaleXProperty(), from + 0.02),
                            new KeyValue(breathOrb.scaleYProperty(), from + 0.02)),
                    new KeyFrame(Duration.seconds(sec),
                            new KeyValue(breathOrb.scaleXProperty(), from),
                            new KeyValue(breathOrb.scaleYProperty(), from))
            );
            currentOrbAnim = pulse;
            pulse.play();
            return;
        }

        ScaleTransition st = new ScaleTransition(Duration.seconds(sec), breathOrb);
        st.setFromX(from);
        st.setFromY(from);
        st.setToX(to);
        st.setToY(to);
        st.setInterpolator(Interpolator.EASE_BOTH);
        currentOrbAnim = st;
        st.play();
    }

    private int currentPhaseDuration(BreathingProtocol p) {
        return switch (phase) {
            case INHALE -> p.getInhale();
            case HOLD1 -> p.getHold1();
            case EXHALE -> p.getExhale();
            case HOLD2 -> p.getHold2();
        };
    }

    private void resetUI() {
        stopAll();

        btnStart.setDisable(false);
        btnStop.setDisable(true);

        ringProgress.setProgress(0);
        progressBar.setProgress(0);

        lblPhaseBig.setText("PRÊT");
        lblPhaseSmall.setText("Choisis un état puis démarre");
        lblPercent.setText("0%");
        lblTimeLeft.setText(currentPlan == null ? "00:00" : formatSeconds(currentPlan.getProtocol().getTotalSeconds()));

        breathOrb.setScaleX(0.88);
        breathOrb.setScaleY(0.88);

        glow.setRadius(0);
    }

    private void stopAll() {
        if (tickTimeline != null) {
            tickTimeline.stop();
            tickTimeline = null;
        }
        if (currentOrbAnim != null) {
            currentOrbAnim.stop();
            currentOrbAnim = null;
        }
    }

    private String formatSeconds(int s) {
        int m = s / 60;
        int r = s % 60;
        return String.format("%02d:%02d", m, r);
    }

    private String stateLabel(BreathingState s) {
        return switch (s) {
            case STRESS_AIGU -> "Stress aigu";
            case FATIGUE -> "Fatigue";
            case RUMINATIONS -> "Ruminations";
            case NORMAL -> "Normal";
        };
    }

    private String goalLabel(BreathingState s) {
        return switch (s) {
            case STRESS_AIGU -> "Anti-stress";
            case FATIGUE -> "Énergie douce";
            case RUMINATIONS -> "Calmer le mental";
            case NORMAL -> "Régulation";
        };
    }

    private void removeStateClasses(Region r) {
        if (r == null) return;
        r.getStyleClass().removeAll("state-stress", "state-fatigue", "state-ruminations", "state-normal");
    }

    private void removeBadgeClasses(Label l) {
        if (l == null) return;
        l.getStyleClass().removeAll("badge-stress", "badge-fatigue", "badge-ruminations", "badge-normal");
    }

    private void removeChipClasses(Label l) {
        if (l == null) return;
        l.getStyleClass().removeAll("chip-stress", "chip-fatigue", "chip-ruminations", "chip-normal");
    }
}