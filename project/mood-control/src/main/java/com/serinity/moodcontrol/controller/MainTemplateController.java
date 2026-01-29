package com.serinity.moodcontrol.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.List;

public class MainTemplateController {

    @FXML private Button btnDashboard;
    @FXML private Button btnSleep;
    @FXML private Button btnMood;
    @FXML private Button btnSupport;
    @FXML private Button btnExercises;
    @FXML private Button btnAppointments;

    @FXML private Label userNameLabel;
    @FXML private ImageView avatarImg;

    @FXML private StackPane contentHost;

    private List<Button> navButtons;

    @FXML
    public void initialize() {
        navButtons = List.of(btnDashboard, btnSleep, btnMood, btnSupport, btnExercises, btnAppointments);

        userNameLabel.setText("7ot_User_Name_lena");

        // Default: Mood page (since you're working there)
        setActiveNav(btnMood);
        loadIntoHost("/fxml/mood/MoodHome.fxml");
    }

    @FXML
    private void onNavClick(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        setActiveNav(clicked);

        if (clicked == btnMood) {
            loadIntoHost("/fxml/mood/MoodHome.fxml");
        } else {
            loadIntoHost("/fxml/pages/Blank.fxml");
        }
    }

    private void setActiveNav(Button activeBtn) {
        for (Button b : navButtons) {
            if (!b.getStyleClass().contains("nav-btn")) {
                b.getStyleClass().add("nav-btn");
            }
            b.getStyleClass().remove("nav-btn-active");
        }
        if (!activeBtn.getStyleClass().contains("nav-btn-active")) {
            activeBtn.getStyleClass().add("nav-btn-active");
        }
    }

    private void loadIntoHost(String fxmlPath) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentHost.getChildren().setAll(page);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load: " + fxmlPath, e);
        }
    }
}
