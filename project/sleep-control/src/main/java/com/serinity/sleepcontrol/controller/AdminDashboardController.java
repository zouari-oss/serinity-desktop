package com.serinity.sleepcontrol.controller;

import com.serinity.sleepcontrol.utils.PowerBIConfig;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;

public class AdminDashboardController {

    // ✅ Doit correspondre au container racine de ton admin-dashboard.fxml (ex: VBox)
    @FXML private Node root;

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
        // ✅ Ferme UNIQUEMENT la fenêtre qui contient ce FXML
        if (root != null && root.getScene() != null && root.getScene().getWindow() != null) {
            ((Stage) root.getScene().getWindow()).close(); // ou .hide()
        }
    }

    private void ouvrirLien(String url, String nom) {
        try {
            if (url == null || url.isBlank() || url.startsWith("COLLE_ICI")) {
                showError(
                        "Lien manquant",
                        "Colle le lien Power BI Service pour " + nom +
                                " dans PowerBIConfig (ADMIN_*_MANAGE_URL)."
                );
                return;
            }

            if (!Desktop.isDesktopSupported()) {
                showError("Erreur", "Impossible d’ouvrir le navigateur sur cette machine.");
                return;
            }

            Desktop.getDesktop().browse(new URI(url));

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