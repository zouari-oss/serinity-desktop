package com.serinity.sleepcontrol.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;

public class MainTemplateController {

    @FXML private StackPane contentHost;
    @FXML private Button btnSleep;
    @FXML private Button btnReve;
    @FXML private Label userNameLabel;
    private Button currentActiveButton;

    @FXML
    public void initialize() {
        userNameLabel.setText("Utilisateur");

        currentActiveButton = btnSleep;
        btnSleep.getStyleClass().add("nav-btn-active");
        loadPage("/view/fxml/sleep-page.fxml");
    }

    @FXML
    private void onNavClick(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-btn-active");
        }

        clickedButton.getStyleClass().add("nav-btn-active");
        currentActiveButton = clickedButton;

        if (clickedButton == btnSleep) {
            loadPage("/view/fxml/sleep-page.fxml");
        } else if (clickedButton == btnReve) {
            loadPage("/view/fxml/reve-page.fxml");
        } else {
            showInfoPage("Page en construction");
        }
    }

    private void loadPage(String fxmlPath) {
        try {
            URL url = getClass().getResource(fxmlPath);

            if (url == null) {
                System.err.println("ERREUR: Fichier FXML introuvable: " + fxmlPath);
                showErrorPage("Page introuvable: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent page = loader.load();

            contentHost.getChildren().clear();
            contentHost.getChildren().add(page);

        } catch (IOException e) {
            System.err.println("ERREUR lors du chargement de " + fxmlPath);
            e.printStackTrace();
            showErrorPage("Erreur: " + e.getMessage());
        }
    }

    private void showErrorPage(String message) {
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #f44336;");
        contentHost.getChildren().clear();
        contentHost.getChildren().add(errorLabel);
    }

    private void showInfoPage(String message) {
        Label infoLabel = new Label(message);
        infoLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #666;");
        contentHost.getChildren().clear();
        contentHost.getChildren().add(infoLabel);
    }
}
