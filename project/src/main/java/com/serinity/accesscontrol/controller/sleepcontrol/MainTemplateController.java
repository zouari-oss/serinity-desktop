package com.serinity.accesscontrol.controller.sleepcontrol;

import com.serinity.accesscontrol.flag.UserRole;
import com.serinity.accesscontrol.controller.LoginController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;

public class MainTemplateController {

    @FXML private StackPane contentHost;

    // Nav buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnMood;
    @FXML private Button btnSupport;
    @FXML private Button btnExercises;
    @FXML private Button btnAppointments;

    // MenuButton Sommeil
    @FXML private MenuButton menuSleep;

    @FXML private Label userNameLabel;

    // Suivi du bouton actif (null si menuSleep est actif)
    private Button currentActiveButton;

    @FXML
    public void initialize() {
        if (userNameLabel != null) {
            userNameLabel.setText("Utilisateur");
        }
    }

    // =========================
    // NAV BUTTONS (Dashboard/Mood/Support/Exercises/Appointments)
    // =========================
    @FXML
    private void onNavClick(ActionEvent event) {
        Button clicked = (Button) event.getSource();

        // ✅ Désactiver le menuSleep (visuellement)
        menuSleep.getStyleClass().remove("nav-menu-btn-active");

        // ✅ Activer le bouton cliqué
        setActiveButton(clicked);

        if (clicked == btnDashboard) {
            loadPage("/fxml/sleepcontrol/admin-dashboard.fxml");
        } else if (clicked == btnMood) {
            showInfoPage("Page Mood — module de vos collègues");
            // si tu as un vrai fxml, remplace par:
        } else if (clicked == btnSupport) {
            showInfoPage("Page Support — module de vos collègues");
            // loadPage("/fxml/sleepcontrol/support.fxml");
        } else if (clicked == btnExercises) {
            showInfoPage("Page Exercises — module de vos collègues");
            // loadPage("/fxml/sleepcontrol/exercices.fxml");
        } else if (clicked == btnAppointments) {
            showInfoPage("Page Appointments — module de vos collègues");
            // loadPage("/fxml/sleepcontrol/appointments.fxml");
        }
    }

    // =========================
    // Helpers
    // =========================
    private void setActiveButton(Button newActive) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-btn-active");
        }
        if (newActive != null && !newActive.getStyleClass().contains("nav-btn-active")) {
            newActive.getStyleClass().add("nav-btn-active");
        }
        currentActiveButton = newActive;
    }

    private void clearActiveButton() {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-btn-active");
            currentActiveButton = null;
        }
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
            page.getStylesheets().add(getClass().getResource("styles/sleepcontrol/styles.css").toExternalForm());
            contentHost.getChildren().setAll(page);

        } catch (IOException e) {
            e.printStackTrace();
            showErrorPage("Erreur de chargement : " + e.getMessage());
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
