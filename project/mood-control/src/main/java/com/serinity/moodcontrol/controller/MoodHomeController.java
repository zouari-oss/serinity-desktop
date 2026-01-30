package com.serinity.moodcontrol.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.ResourceBundle;

public class MoodHomeController {

    @FXML private StackPane moodHost;

    @FXML private StackPane cardLogMood;
    @FXML private StackPane cardMoodHistory;
    @FXML private StackPane cardJournal;

    //  this is the ResourceBundle used to load MoodHome.fxml
    @FXML private ResourceBundle resources;

    @FXML
    public void initialize() {
        cardLogMood.setOnMouseClicked(e -> loadWizard());
        cardMoodHistory.setOnMouseClicked(e -> loadHistory());
        // Journal intentionally does nothing (coming soon)
    }

    private void loadWizard() {
        loadIntoHost("/fxml/mood/Wizard.fxml");
    }

    private void loadHistory() {
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

    private void loadIntoHost(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml), resources);
            Parent view = loader.load();
            moodHost.getChildren().setAll(view);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + fxml, e);
        }
    }
}
