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

  @FXML
  private Label stepTitle;
  @FXML
  private Label stepCounter;
  @FXML
  private StackPane cardHost;
  @FXML
  private Button btnBack;
  @FXML
  private Button btnNext;

  @FXML
  private ResourceBundle resources;

  // stakpane li bech tet3adelha
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

  // inject host (MoodHomeController / MoodHistoryController must call this)
  public void setMoodHost(final StackPane moodHost) {
    this.moodHost = moodHost;
  }

  public void setOnFinish(final Runnable onFinish) {
    this.onFinish = onFinish;
  }

  // called from History when clicking Edit
  public void startEdit(final MoodEntry entry) {
    this.editingEntry = entry;
    this.editMode = (entry != null && entry.getId() > 0);
    loadStep(stepIndex, stepIndex, false);
  }

  public void setCanGoNext(final boolean canGoNext) {
    btnNext.setDisable(!canGoNext);
  }

  @FXML
  private void onBack() {
    if (stepIndex <= 0)
      return;

    final int oldIndex = stepIndex;
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

    final int oldIndex = stepIndex;
    stepIndex++;
    refreshUI();
    loadStep(oldIndex, stepIndex, true);
  }

  private void refreshUI() {
    // Step counter (pattern-based)
    final String counterPattern = resources.getString("wizard.counter");
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

  private void loadStep(final int oldIndex, final int newIndex, final boolean animate) {
    try {
      Node nextView;

      if (newIndex == 0) {
        final FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/mood/steps/StepType.fxml"),
            resources);
        nextView = loader.load();

        stepTypeController = loader.getController();
        stepTypeController.setWizard(this);

        // PREFILL
        if (editMode && editingEntry != null) {
          stepTypeController.setSelectedType(editingEntry.getMomentType());
        }

        setCanGoNext(stepTypeController.getSelectedType() != null);

      } else if (newIndex == 1) {
        final FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/mood/steps/StepMood.fxml"),
            resources);
        nextView = loader.load();

        stepMoodController = loader.getController();

        // PREFILL
        if (editMode && editingEntry != null) {
          stepMoodController.setMoodLevel(editingEntry.getMoodLevel());
        }

        setCanGoNext(true);

      } else if (newIndex == 2) {
        final FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/mood/steps/StepEmotions.fxml"),
            resources);
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
        final FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/mood/steps/StepInfluences.fxml"),
            resources);
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
            MessageFormat.format(resources.getString("wizard.step.unknown"), newIndex + 1)));
        setCanGoNext(true);
      }

      if (!animate || cardHost.getChildren().isEmpty()) {
        cardHost.getChildren().setAll(nextView);
        return;
      }

      final boolean forward = newIndex > oldIndex;
      animateSwap(nextView, forward);

    } catch (final IOException e) {
      throw new RuntimeException("Failed to load step view", e);
    }
  }

  private void saveToDbAndFinish() {
    try {
      final MoodEntry entry = new MoodEntry();

      entry.setUserId(1); // TEMP
      entry.setMomentType(stepTypeController.getSelectedType());
      entry.setMoodLevel(stepMoodController.getMoodLevel());
      entry.setEmotions(stepEmotionsController.getSelectedEmotions());
      entry.setInfluences(stepInfluencesController.getSelectedInfluences());

      final MoodEntryDao dao = new MoodEntryDao();

      if (editMode && editingEntry != null) {
        entry.setId(editingEntry.getId());
        dao.update(entry);
        System.out.println("Updated mood entry: id=" + entry.getId());
      } else {
        final long id = dao.save(entry);
        System.out.println("Saved mood entry: id=" + id);
      }

      // keep old behavior (History refresh hook)
      if (onFinish != null)
        onFinish.run();

      //  ALWAYS navigate to MoodHistory after finish
      goToHistory();

    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  //  navigate to history inside same host
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
      final FXMLLoader loader = new FXMLLoader(
          getClass().getResource("/fxml/mood/MoodHistory.fxml"),
          resources);
      final Parent view = loader.load();

      final MoodHistoryController c = loader.getController();
      c.setMoodHost(moodHost);

      moodHost.getChildren().setAll(view);

    } catch (final IOException e) {
      throw new RuntimeException("Failed to load /fxml/mood/MoodHistory.fxml", e);
    }
  }

  private void animateSwap(final Node nextView, final boolean forward) {
    final Node current = cardHost.getChildren().get(0);

    final double width = 520;

    nextView.setTranslateX(forward ? width : -width);
    cardHost.getChildren().add(nextView);

    final TranslateTransition out = new TranslateTransition(Duration.millis(180), current);
    out.setToX(forward ? -width : width);

    final TranslateTransition in = new TranslateTransition(Duration.millis(180), nextView);
    in.setToX(0);

    out.play();
    in.play();

    in.setOnFinished(e -> {
      cardHost.getChildren().remove(current);
      current.setTranslateX(0);
    });
  }
} // StateOfMindWizardController class
