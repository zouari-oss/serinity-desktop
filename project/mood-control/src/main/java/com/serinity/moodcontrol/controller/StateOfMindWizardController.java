package com.serinity.moodcontrol.controller;

import com.serinity.moodcontrol.dao.MoodEntryDao;
import com.serinity.moodcontrol.model.MoodEntry;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;

public class StateOfMindWizardController {

    @FXML private Label stepTitle;
    @FXML private Label stepCounter;
    @FXML private StackPane cardHost;
    @FXML private Button btnBack;
    @FXML private Button btnNext;

    private int stepIndex = 0;
    private final int totalSteps = 4;

    private StepTypeController stepTypeController;
    private StepMoodController stepMoodController;
    private StepEmotionsController stepEmotionsController;
    private StepInfluencesController stepInfluencesController;

    // edit mode state
    private MoodEntry editingEntry;
    private boolean editMode = false;

    private Runnable onFinish;

    @FXML
    public void initialize() {
        btnNext.setDisable(true);
        refreshUI();
        loadStep(0, 0, false);
    }

    public void setOnFinish(Runnable onFinish) {
        this.onFinish = onFinish;
    }

    // called from History when clicking Edit
    public void startEdit(MoodEntry entry) {
        this.editingEntry = entry;
        this.editMode = (entry != null && entry.getId() > 0);
        // optional: if wizard already on screen, reload current step to apply prefill
        loadStep(stepIndex, stepIndex, false);
    }

    @FXML
    private void onBack() {
        if (stepIndex <= 0) return;

        int oldIndex = stepIndex;
        stepIndex--;

        refreshUI();
        loadStep(oldIndex, stepIndex, true);
    }

    @FXML
    private void onNext() {
        if (stepIndex == totalSteps - 1) {
            saveToDbAndFinish();
            return;
        }

        int oldIndex = stepIndex;
        stepIndex++;
        refreshUI();
        loadStep(oldIndex, stepIndex, true);
    }

    public void setCanGoNext(boolean canGoNext) {
        btnNext.setDisable(!canGoNext);
    }

    private void refreshUI() {
        stepCounter.setText("Step " + (stepIndex + 1) + " of " + totalSteps);
        btnBack.setDisable(stepIndex == 0);

        if (stepIndex == 0) {
            stepTitle.setText("State of Mind • Type");
            btnNext.setText("Next");
        } else if (stepIndex == 1) {
            stepTitle.setText("State of Mind • Mood");
            btnNext.setText("Next");
        } else if (stepIndex == 2) {
            stepTitle.setText("State of Mind • Emotions");
            btnNext.setText("Next");
        } else if (stepIndex == 3) {
            stepTitle.setText("State of Mind • Influences");
            btnNext.setText("Finish");
        } else {
            stepTitle.setText("State of Mind");
            btnNext.setText("Next");
        }
    }

    private void loadStep(int oldIndex, int newIndex, boolean animate) {
        try {
            Node nextView;

            if (newIndex == 0) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mood/steps/StepType.fxml"));
                nextView = loader.load();

                stepTypeController = loader.getController();
                stepTypeController.setWizard(this);

                // ✅ PREFILL
                if (editMode && editingEntry != null) {
                    stepTypeController.setSelectedType(editingEntry.getMomentType());
                }

                setCanGoNext(stepTypeController.getSelectedType() != null);

            } else if (newIndex == 1) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mood/steps/StepMood.fxml"));
                nextView = loader.load();

                stepMoodController = loader.getController();

                // ✅ PREFILL (requires StepMoodController.setMoodLevel(int))
                if (editMode && editingEntry != null) {
                    stepMoodController.setMoodLevel(editingEntry.getMoodLevel());
                }

                setCanGoNext(true);

            } else if (newIndex == 2) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mood/steps/StepEmotions.fxml"));
                nextView = loader.load();

                stepEmotionsController = loader.getController();
                stepEmotionsController.setWizard(this);

                // ✅ PREFILL (requires StepEmotionsController.setSelectedEmotions(List<String>))
                if (editMode && editingEntry != null) {
                    stepEmotionsController.setSelectedEmotions(editingEntry.getEmotions());
                }

                // StepEmotions enforces "at least 1 selected" -> enable next accordingly
                setCanGoNext(stepEmotionsController.getSelectedEmotions() != null
                        && !stepEmotionsController.getSelectedEmotions().isEmpty());

            } else if (newIndex == 3) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mood/steps/StepInfluences.fxml"));
                nextView = loader.load();

                stepInfluencesController = loader.getController();
                stepInfluencesController.setWizard(this);

                // ✅ PREFILL (requires StepInfluencesController.setSelectedInfluences(List<String>))
                if (editMode && editingEntry != null) {
                    stepInfluencesController.setSelectedInfluences(editingEntry.getInfluences());
                }

                setCanGoNext(stepInfluencesController.getSelectedInfluences() != null
                        && !stepInfluencesController.getSelectedInfluences().isEmpty());

            } else {
                nextView = new StackPane(new Label("Step " + (newIndex + 1) + " coming next…"));
                setCanGoNext(true);
            }

            if (!animate || cardHost.getChildren().isEmpty()) {
                cardHost.getChildren().setAll(nextView);
                return;
            }

            boolean forward = newIndex > oldIndex;
            animateSwap(nextView, forward);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load step view", e);
        }
    }

    private void saveToDbAndFinish() {
        try {
            MoodEntry entry = new MoodEntry();

            entry.setUserId(1); // TEMP
            entry.setMomentType(stepTypeController.getSelectedType());
            entry.setMoodLevel(stepMoodController.getMoodLevel());
            entry.setEmotions(stepEmotionsController.getSelectedEmotions());
            entry.setInfluences(stepInfluencesController.getSelectedInfluences());

            MoodEntryDao dao = new MoodEntryDao();

            if (editMode && editingEntry != null) {
                entry.setId(editingEntry.getId());
                dao.update(entry); // must exist
                System.out.println("Updated mood entry: id=" + entry.getId());
            } else {
                long id = dao.save(entry);
                System.out.println("Saved mood entry: id=" + id);
            }

            if (onFinish != null) onFinish.run();

            // mimic “switch tab then come back” (reload MoodHome)
            Scene scene = btnNext.getScene();
            StackPane host = (StackPane) scene.lookup("#contentHost");
            if (host == null) {
                throw new IllegalStateException("contentHost not found. Did you set fx:id=\"contentHost\" in MainTemplate.fxml?");
            }

            Parent moodHome = FXMLLoader.load(getClass().getResource("/fxml/mood/MoodHome.fxml"));
            animateHostSwap(host, moodHome);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animateSwap(Node nextView, boolean forward) {
        Node current = cardHost.getChildren().get(0);

        double width = 520;

        nextView.setTranslateX(forward ? width : -width);
        cardHost.getChildren().add(nextView);

        TranslateTransition out = new TranslateTransition(Duration.millis(180), current);
        out.setToX(forward ? -width : width);

        TranslateTransition in = new TranslateTransition(Duration.millis(180), nextView);
        in.setToX(0);

        out.play();
        in.play();

        in.setOnFinished(e -> {
            cardHost.getChildren().remove(current);
            current.setTranslateX(0);
        });
    }

    private void animateHostSwap(StackPane host, Parent nextView) {
        if (host == null) return;

        if (host.getChildren().isEmpty()) {
            host.getChildren().setAll(nextView);
            nextView.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(180), nextView);
            fadeIn.setToValue(1);
            fadeIn.play();
            return;
        }

        Node current = host.getChildren().get(0);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(160), current);
        fadeOut.setToValue(0);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(160), current);
        slideOut.setToX(-28);

        ParallelTransition out = new ParallelTransition(fadeOut, slideOut);

        out.setOnFinished(e -> {
            current.setOpacity(1);
            current.setTranslateX(0);

            nextView.setOpacity(0);
            nextView.setTranslateX(12);
            host.getChildren().setAll(nextView);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(180), nextView);
            fadeIn.setToValue(1);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(180), nextView);
            slideIn.setToX(0);

            new ParallelTransition(fadeIn, slideIn).play();
        });

        out.play();
    }
}
