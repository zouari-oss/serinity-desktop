package com.serinity.sleepcontrol.controller;

import com.serinity.sleepcontrol.utils.PowerBIConfig;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;

public class AdminDashboardController {

    @FXML
    private void gererSommeil() {
        ouvrirLien(PowerBIConfig.ADMIN_SOMMEIL_MANAGE_URL, "Sommeil");
    }

    @FXML
    private void gererReve() {
        ouvrirLien(PowerBIConfig.ADMIN_REVE_MANAGE_URL, "Rêve");
    }

    @FXML
    private void retour() {
        // Ferme la fenêtre si c’est une popup
        Stage stage = (Stage) javafx.stage.Window.getWindows().stream()
                .filter(w -> w.isShowing())
                .reduce((first, second) -> second)
                .orElse(null);

        if (stage != null) stage.hide();
    }

    private void ouvrirLien(String url, String nom) {
        try {
            if (url == null || url.isBlank() || url.startsWith("COLLE_ICI")) {
                showError("Lien manquant",
                        "Colle le lien Power BI Service pour " + nom + " dans PowerBIConfig (ADMIN_*_MANAGE_URL).");
                return;
            }

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                showError("Erreur", "Impossible d’ouvrir le navigateur sur cette machine.");
            }
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir Power BI (" + nom + ").");
            e.printStackTrace();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}