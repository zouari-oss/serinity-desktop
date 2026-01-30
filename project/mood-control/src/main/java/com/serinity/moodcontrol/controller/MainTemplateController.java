package com.serinity.moodcontrol.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import java.util.Locale;
import java.util.ResourceBundle;
import java.text.MessageFormat;
import javafx.scene.control.TextField;


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
    @FXML private TextField searchField;   // you already have it in FXML
    @FXML private Label footerLabel;       // we’ll add fx:id to footer label
    @FXML private ResourceBundle resources;

    private ResourceBundle bundle;

    private List<Button> navButtons;

    @FXML
    public void initialize() {

        if (resources == null) {
            throw new IllegalStateException("Bundle not injected");
        }

        footerLabel.setText(resources.getString("footer.text"));

        navButtons = List.of(
                btnDashboard, btnSleep, btnMood,
                btnSupport, btnExercises, btnAppointments
        );

        userNameLabel.setText("7ot_User_Name_lena");

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
            // resources is injected into this controller because Template.fxml was loaded with a bundle
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), resources);
            Parent page = loader.load();
            contentHost.getChildren().setAll(page);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load: " + fxmlPath, e);
        }
    }

}
