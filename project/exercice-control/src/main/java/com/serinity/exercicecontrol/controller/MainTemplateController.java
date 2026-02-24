package com.serinity.exercicecontrol.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainTemplateController {

    @FXML private StackPane contentHost;

    @FXML private Button btnDashboard;
    @FXML private Button btnSleep;
    @FXML private Button btnMood;
    @FXML private Button btnSupport;
    @FXML private Button btnExercises;
    @FXML private Button btnAppointments;

    @FXML private Label footerLabel;
    @FXML private Label userNameLabel;

    // ✅ same bundle as App.java
    private final ResourceBundle bundle =
            ResourceBundle.getBundle("i18n.messages", Locale.getDefault());

    @FXML
    public void initialize() {
        userNameLabel.setText("User");

        updateFooter();
        setActive(btnExercises);

        // Default page on launch
        loadPage("exercice/ExerciseList.fxml");
    }

    @FXML
    public void onNavClick(ActionEvent event) {
        Object src = event.getSource();

        if (src == btnDashboard) {
            setActive(btnDashboard);
            loadPage("Blank.fxml");
        } else if (src == btnSleep) {
            setActive(btnSleep);
            loadPage("Blank.fxml");
        } else if (src == btnMood) {
            setActive(btnMood);
            loadPage("Blank.fxml");
        } else if (src == btnSupport) {
            setActive(btnSupport);
            loadPage("Blank.fxml");
        } else if (src == btnExercises) {
            setActive(btnExercises);
            loadPage("exercice/ExerciseList.fxml");
        } else if (src == btnAppointments) {
            setActive(btnAppointments);
            loadPage("Blank.fxml");
        }

        updateFooter();
    }

    private void loadPage(String fxmlRelativePath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/" + fxmlRelativePath),
                    bundle
            );
            Parent page = loader.load();
            contentHost.getChildren().setAll(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActive(Button active) {
        clearActive(btnDashboard, btnSleep, btnMood, btnSupport, btnExercises, btnAppointments);
        active.getStyleClass().add("nav-btn-active");
    }

    private void clearActive(Button... buttons) {
        for (Button b : buttons) {
            b.getStyleClass().remove("nav-btn-active");
        }
    }

    private void updateFooter() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        footerLabel.setText("Serinity — Exercice Module • " + time);
    }
}
