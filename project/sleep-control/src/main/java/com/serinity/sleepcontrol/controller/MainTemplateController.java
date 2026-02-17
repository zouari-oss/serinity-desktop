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
        // Définir le nom d'utilisateur
        userNameLabel.setText("Utilisateur");

        // Charger la page Sommeil par défaut
        currentActiveButton = btnSleep;
        btnSleep.getStyleClass().add("nav-btn-active");
        loadPage("/view/fxml/sleep-page.fxml");
    }

    @FXML
    private void onNavClick(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        // Retirer la classe active de l'ancien bouton
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-btn-active");
        }

        // Ajouter la classe active au nouveau bouton
        clickedButton.getStyleClass().add("nav-btn-active");
        currentActiveButton = clickedButton;

        // Charger la page correspondante
        if (clickedButton == btnSleep) {
            loadPage("/view/fxml/sleep-page.fxml");
        } else if (clickedButton == btnReve) {
            loadPage("/view/fxml/reve-page.fxml");
        } else {
            // Pages non implémentées
            showInfoPage("Page en construction");
        }
    }

    /**
     * Charge une page FXML dans le contentHost
     */
    private void loadPage(String fxmlPath) {
        try {
            // Obtenir l'URL de la ressource
            URL url = getClass().getResource(fxmlPath);

            if (url == null) {
                System.err.println("ERREUR: Fichier FXML introuvable: " + fxmlPath);
                showErrorPage("Page introuvable: " + fxmlPath);
                return;
            }

            // Charger le FXML
            FXMLLoader loader = new FXMLLoader(url);
            Parent page = loader.load();

            // Afficher dans le contentHost
            contentHost.getChildren().clear();
            contentHost.getChildren().add(page);

        } catch (IOException e) {
            System.err.println("ERREUR lors du chargement de " + fxmlPath);
            e.printStackTrace();
            showErrorPage("Erreur: " + e.getMessage());
        }
    }

    /**
     * Affiche une page d'erreur
     */
    private void showErrorPage(String message) {
        Label errorLabel = new Label("❌ " + message);
        errorLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #f44336;");
        contentHost.getChildren().clear();
        contentHost.getChildren().add(errorLabel);
    }

    /**
     * Affiche une page d'info
     */
    private void showInfoPage(String message) {
        Label infoLabel = new Label("ℹ️ " + message);
        infoLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #666;");
        contentHost.getChildren().clear();
        contentHost.getChildren().add(infoLabel);
    }
}
