package com.serinity.sleepcontrol.controller;

import com.serinity.sleepcontrol.utils.PowerBIConfig;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;

public class AdminDashboardController {

    // ✅ Doit correspondre au vrai root du FXML (StackPane recommandé)
    @FXML
    private Node root;

    // ✅ Chemin ABSOLU classpath (resources/view/styles/styles.css)
    private static final String CSS_PATH = "/view/styles/styles.css";

    @FXML
    public void initialize() {
        // Force le CSS une fois que la Scene est prête
        Platform.runLater(this::forceCss);
    }

    private void forceCss() {
        try {
            if (root == null || root.getScene() == null) return;

            Scene scene = root.getScene();

            var cssUrl = getClass().getResource(CSS_PATH);
            if (cssUrl == null) {
                System.err.println("❌ CSS introuvable: " + CSS_PATH);
                return;
            }

            String css = cssUrl.toExternalForm();

            // évite les doublons
            if (!scene.getStylesheets().contains(css)) {
                scene.getStylesheets().add(css);
            }

            // debug
            System.out.println("✅ Stylesheets chargés: " + scene.getStylesheets());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===============================
    // ACTIONS BOUTONS
    // ===============================

    @FXML
    private void gererSommeil() {
        ouvrirLien(PowerBIConfig.ADMIN_SOMMEIL_MANAGE_URL, "Statistiques Sommeil");
    }

    @FXML
    private void gererReve() {
        ouvrirLien(PowerBIConfig.ADMIN_REVE_MANAGE_URL, "Statistiques Rêve");
    }

    @FXML
    private void retour() {
        try {
            if (root != null && root.getScene() != null && root.getScene().getWindow() != null) {
                ((Stage) root.getScene().getWindow()).close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===============================
    // LOGIQUE OUVERTURE POWER BI
    // ===============================

    private void ouvrirLien(String url, String nomSection) {
        try {
            if (url == null || url.isBlank() || url.contains("COLLE_ICI")) {
                afficherErreur(
                        "Lien Power BI manquant",
                        "Le lien Power BI pour \"" + nomSection + "\" n'est pas configuré dans PowerBIConfig."
                );
                return;
            }

            if (!Desktop.isDesktopSupported()) {
                afficherErreur("Erreur système", "Impossible d’ouvrir le navigateur sur cette machine.");
                return;
            }

            Desktop.getDesktop().browse(new URI(url));

        } catch (Exception e) {
            afficherErreur("Erreur d’ouverture", "Impossible d'ouvrir Power BI pour \"" + nomSection + "\".");
            e.printStackTrace();
        }
    }

    // ===============================
    // ALERT UTILITAIRE
    // ===============================

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}