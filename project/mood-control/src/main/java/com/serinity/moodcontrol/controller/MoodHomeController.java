package com.serinity.moodcontrol.controller;

import java.io.IOException;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public class MoodHomeController {

  @FXML
  private StackPane moodHost;
  @FXML
  private StackPane cardLogMood;
  @FXML
  private StackPane cardMoodHistory;
  @FXML
  private StackPane cardJournal;
  @FXML
  private ResourceBundle resources;

  @FXML
  public void initialize() {
    if (resources == null) {
      throw new IllegalStateException("ResourceBundle not injected for MoodHome. Load MoodHome.fxml with a bundle.");
    }

    cardLogMood.setOnMouseClicked(e -> loadWizard());
    cardMoodHistory.setOnMouseClicked(e -> loadHistory());
    cardJournal.setOnMouseClicked(e -> loadJournal());

  }

  private void loadWizard() {
    try {
      final FXMLLoader loader = new FXMLLoader(
          getClass().getResource("/fxml/mood/Wizard.fxml"),
          resources);
      final Parent view = loader.load();

      final StateOfMindWizardController wiz = loader.getController();
      wiz.setMoodHost(moodHost); // THIS is the missing link

      moodHost.getChildren().setAll(view);

    } catch (final IOException e) {
      throw new RuntimeException("Failed to load /fxml/mood/Wizard.fxml", e);
    }
  }

  private void loadHistory() {
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
    private void loadJournal() {
        try {
            final FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/mood/Journal.fxml"),
                    resources
            );

            final Parent view = loader.load();

            moodHost.getChildren().setAll(view);

        } catch (final IOException e) {
            throw new RuntimeException("Failed to load /fxml/mood/Journal.fxml", e);
        }
    }


} // MoodHistoryController class
