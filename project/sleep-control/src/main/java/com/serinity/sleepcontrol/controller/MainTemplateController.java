package com.serinity.sleepcontrol.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;

public class MainTemplateController {

    @FXML private StackPane contentHost;

    // Les 5 boutons du groupe (btnSleep est remplacé par menuSleep)
    @FXML private Button btnDashboard;
    @FXML private Button btnMood;
    @FXML private Button btnSupport;
    @FXML private Button btnExercises;
    @FXML private Button btnAppointments;

    // Ton MenuButton à la place de btnSleep
    @FXML private MenuButton menuSleep;

    @FXML private Label userNameLabel;

    // Suivi du bouton actif (null si c'est menuSleep qui est actif)
    private Button currentActiveButton;

    @FXML
    public void initialize() {
        if (userNameLabel != null) {
            userNameLabel.setText("Utilisateur");
        }

        // Dashboard actif par défaut
        currentActiveButton = btnDashboard;
        setActiveButton(btnDashboard);

        loadPage("/view/fxml/sleep-page.fxml");
    }

    @FXML
    private void onNavClick(ActionEvent event) {
        Button clicked = (Button) event.getSource();

        // Désactiver menuSleep si actif
        menuSleep.getStyleClass().remove("nav-menu-btn-active");

        setActiveButton(clicked);

        if (clicked == btnDashboard) {
            loadPage("/view/fxml/sleep-page.fxml");
        } else if (clicked == btnMood) {
            showInfoPage("Page Mood — module de vos collègues");
        } else if (clicked == btnSupport) {
            showInfoPage("Page Support — module de vos collègues");
        } else if (clicked == btnExercises) {
            showInfoPage("Page Exercises — module de vos collègues");
        } else if (clicked == btnAppointments) {
            showInfoPage("Page Appointments — module de vos collègues");
        }
    }

    @FXML
    private void onMenuSommeil() {
        // Désactiver les boutons normaux
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-btn-active");
            currentActiveButton = null;
        }
        menuSleep.getStyleClass().remove("nav-menu-btn-active");
        menuSleep.getStyleClass().add("nav-menu-btn-active");

        loadPage("/view/fxml/sleep-page.fxml");
    }

    @FXML
    private void onMenuReve() {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-btn-active");
            currentActiveButton = null;
        }
        menuSleep.getStyleClass().remove("nav-menu-btn-active");
        menuSleep.getStyleClass().add("nav-menu-btn-active");

        loadPage("/view/fxml/reve-page.fxml");
    }

    private void setActiveButton(Button newActive) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-btn-active");
        }
        if (newActive != null && !newActive.getStyleClass().contains("nav-btn-active")) {
            newActive.getStyleClass().add("nav-btn-active");
        }
        currentActiveButton = newActive;
    }

    private void loadPage(String fxmlPath) {
        try {
            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                showErrorPage("Page introuvable : " + fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent page = loader.load();
            contentHost.getChildren().clear();
            contentHost.getChildren().add(page);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorPage("Erreur : " + e.getMessage());
        }
    }

    private void showErrorPage(String message) {
        Label l = new Label(message);
        l.setStyle("-fx-font-size: 16px; -fx-text-fill: #f44336;");
        contentHost.getChildren().setAll(l);
    }

    private void showInfoPage(String message) {
        Label l = new Label(message);
        l.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
        contentHost.getChildren().setAll(l);
    }
}
