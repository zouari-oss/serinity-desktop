package com.serinity.moodcontrol.controller;

import com.serinity.moodcontrol.dao.MoodEntryDao;
import com.serinity.moodcontrol.model.MoodEntry;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

public class StateOfMindWizardController {

    @FXML private Label stepTitle;
    @FXML private Label stepCounter;
    @FXML private StackPane cardHost;
    @FXML private Button btnBack;
    @FXML private Button btnNext;

    @FXML private ResourceBundle resources;

    // ✅ host to navigate after finish
    private StackPane moodHost;

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
        if (resources == null) {
            throw new IllegalStateException("ResourceBundle not injected for Wizard. Load Wizard.fxml with a bundle.");
        }

        btnNext.setDisable(true);
        refreshUI();
        loadStep(0, 0, false);
    }

    // ✅ inject host (MoodHomeController / MoodHistoryController must call this)
    public void setMoodHost(StackPane moodHost) {
        this.moodHost = moodHost;
    }

    public void setOnFinish(Runnable onFinish) {
        this.onFinish = onFinish;
    }

    // called from History when clicking Edit
    public void startEdit(MoodEntry entry) {
        this.editingEntry = entry;
        this.editMode = (entry != null && entry.getId() > 0);
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
        // Step counter (pattern-based)
        String counterPattern = resources.getString("wizard.counter");
        stepCounter.setText(MessageFormat.format(counterPattern, stepIndex + 1, totalSteps));

        btnBack.setDisable(stepIndex == 0);

        // Step title per step (Java 11 compatible)
        switch (stepIndex) {
            case 0:
                stepTitle.setText(resources.getString("wizard.step.type.title"));
                break;
            case 1:
                stepTitle.setText(resources.getString("wizard.step.mood.title"));
                break;
            case 2:
                stepTitle.setText(resources.getString("wizard.step.emotions.title"));
                break;
            case 3:
                stepTitle.setText(resources.getString("wizard.step.influences.title"));
                break;
            default:
                stepTitle.setText(resources.getString("wizard.title"));
                break;
        }

        // Next / Finish button
        if (stepIndex == totalSteps - 1) {
            btnNext.setText(resources.getString("btn.finish"));
        } else {
            btnNext.setText(resources.getString("btn.next"));
        }
    }

    private void loadStep(int oldIndex, int newIndex, boolean animate) {
        try {
            Node nextView;

            if (newIndex == 0) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/mood/steps/StepType.fxml"),
                        resources
                );
                nextView = loader.load();

                stepTypeController = loader.getController();
                stepTypeController.setWizard(this);

                // PREFILL
                if (editMode && editingEntry != null) {
                    stepTypeController.setSelectedType(editingEntry.getMomentType());
                }

                setCanGoNext(stepTypeController.getSelectedType() != null);

            } else if (newIndex == 1) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/mood/steps/StepMood.fxml"),
                        resources
                );
                nextView = loader.load();

                stepMoodController = loader.getController();

                // PREFILL
                if (editMode && editingEntry != null) {
                    stepMoodController.setMoodLevel(editingEntry.getMoodLevel());
                }

                setCanGoNext(true);

            } else if (newIndex == 2) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/mood/steps/StepEmotions.fxml"),
                        resources
                );
                nextView = loader.load();

                stepEmotionsController = loader.getController();
                stepEmotionsController.setWizard(this);

                // PREFILL
                if (editMode && editingEntry != null) {
                    stepEmotionsController.setSelectedEmotions(editingEntry.getEmotions());
                }

                setCanGoNext(stepEmotionsController.getSelectedEmotions() != null
                        && !stepEmotionsController.getSelectedEmotions().isEmpty());

            } else if (newIndex == 3) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/mood/steps/StepInfluences.fxml"),
                        resources
                );
                nextView = loader.load();

                stepInfluencesController = loader.getController();
                stepInfluencesController.setWizard(this);

                // PREFILL
                if (editMode && editingEntry != null) {
                    stepInfluencesController.setSelectedInfluences(editingEntry.getInfluences());
                }

                setCanGoNext(stepInfluencesController.getSelectedInfluences() != null
                        && !stepInfluencesController.getSelectedInfluences().isEmpty());

            } else {
                nextView = new StackPane(new Label(
                        MessageFormat.format(resources.getString("wizard.step.unknown"), newIndex + 1)
                ));
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
                dao.update(entry);
                System.out.println("Updated mood entry: id=" + entry.getId());
            } else {
                long id = dao.save(entry);
                System.out.println("Saved mood entry: id=" + id);
            }

            // keep old behavior (History refresh hook)
            if (onFinish != null) onFinish.run();

            // ✅ ALWAYS navigate to MoodHistory after finish
            goToHistory();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ navigate to history inside same host
    private void goToHistory() {
        if (moodHost == null) {
            System.out.println("[Wizard] moodHost is null → cannot navigate to history.");
            return;
        }
        if (resources == null) {
            System.out.println("[Wizard] resources is null → cannot load history with bundle.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/mood/MoodHistory.fxml"),
                    resources
            );
            Parent view = loader.load();

            MoodHistoryController c = loader.getController();
            c.setMoodHost(moodHost);

            moodHost.getChildren().setAll(view);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load /fxml/mood/MoodHistory.fxml", e);
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
}
