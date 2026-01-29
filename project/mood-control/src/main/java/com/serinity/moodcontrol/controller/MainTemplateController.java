package com.serinity.moodcontrol.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;

import java.util.List;

public class MainTemplateController {

    @FXML private Button btnDashboard;
    @FXML private Button btnSleep;
    @FXML private Button btnMood;
    @FXML private Button btnSupport;
    @FXML private Button btnExercises;
    @FXML private Button btnAppointments;
    @FXML private Button btnExport;

    private List<Button> navButtons;

    @FXML private Label userNameLabel;
    @FXML private ImageView avatarImg;

    @FXML private Label pageTitle;
    @FXML private Label pageHint;
    @FXML private TableView<?> tableView;

    @FXML
    public void initialize() {
        navButtons = List.of(btnDashboard, btnSleep, btnMood, btnSupport, btnExercises, btnAppointments);


        // default
        setActiveNav(btnDashboard);
        loadDashboard();

        // optional: set a test username
        userNameLabel.setText("7ot_User_Name_lena");
    }

    @FXML
    private void onNavClick(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        setActiveNav(clicked);

        if (clicked == btnDashboard) loadDashboard();
        else if (clicked == btnSleep) loadSleep();
        else if (clicked == btnMood) loadMood();
        else if (clicked == btnSupport) loadSupport();
        else if (clicked == btnExercises) loadExercises();
        else if (clicked == btnAppointments) loadAppointments();
    }

    private void setActiveNav(Button activeBtn) {
        for (Button b : navButtons) {
            // make sure base class always exists
            if (!b.getStyleClass().contains("nav-btn")) {
                b.getStyleClass().add("nav-btn");
            }
            b.getStyleClass().remove("nav-btn-active");
        }
        if (!activeBtn.getStyleClass().contains("nav-btn-active")) {
            activeBtn.getStyleClass().add("nav-btn-active");
        }
    }

    private void loadDashboard() {
        pageTitle.setText("Dashboard");
        pageHint.setText("Overview & Account Info");
        tableView.getItems().clear();
    }

    private void loadSleep() {
        pageTitle.setText("Sleep");
        pageHint.setText("Sleep tracking & analysis");
        tableView.getItems().clear();
    }

    private void loadMood() {
        pageTitle.setText("Mood");
        pageHint.setText("Mood entries & Journal");
        tableView.getItems().clear();
    }

    private void loadSupport() {
        pageTitle.setText("Support");
        pageHint.setText("Community & forum");
        tableView.getItems().clear();
    }

    private void loadExercises() {
        pageTitle.setText("Exercises");
        pageHint.setText("Guided exercises & resources");
        tableView.getItems().clear();
    }

    private void loadAppointments() {
        pageTitle.setText("Appointments");
        pageHint.setText("Scheduling & consultations");
        tableView.getItems().clear();
    }
}
