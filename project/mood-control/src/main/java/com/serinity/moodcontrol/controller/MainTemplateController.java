package com.serinity.moodcontrol.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

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
    @FXML private TextField searchField;

    @FXML private Label footerLabel;

    // Injected automatically when Template.fxml is loaded with a bundle
    @FXML private ResourceBundle resources;

    private List<Button> navButtons;

    @FXML
    public void initialize() {
        if (resources == null) {
            throw new IllegalStateException("ResourceBundle not injected. Make sure Template.fxml is loaded with a bundle.");
        }

        navButtons = List.of(
                btnDashboard, btnSleep, btnMood,
                btnSupport, btnExercises, btnAppointments
        );

        // Footer text from bundle
        footerLabel.setText(resources.getString("footer.text"));

        // Placeholder username (replace later with real user session)
        userNameLabel.setText(resources.getString("user.name.placeholder"));

        // Default landing (template: always load blank)
        setActiveNav(btnMood);
        loadIntoHost("/fxml/Blank.fxml");
    }

    @FXML
    private void onNavClick(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        setActiveNav(clicked);

        // Template behavior: always load blank for all tabs
        loadIntoHost("/fxml/Blank.fxml");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), resources);
            Parent page = loader.load();
            contentHost.getChildren().setAll(page);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load: " + fxmlPath, e);
        }
    }
}
