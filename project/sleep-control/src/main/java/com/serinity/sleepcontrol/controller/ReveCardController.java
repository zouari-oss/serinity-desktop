package com.serinity.sleepcontrol.controller;

import com.serinity.sleepcontrol.model.Reve;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Contrôleur pour la carte d'affichage d'un rêve
 */
public class ReveCardController {

    @FXML private Label titreLabel;
    @FXML private Label typeLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label humeurLabel;
    @FXML private ProgressBar intensiteBar;
    @FXML private Label intensiteValue;
    @FXML private Label anxieteLabel;
    @FXML private Label couleurBadge;
    @FXML private Label recurrentBadge;
    @FXML private Label lucideBadge;
    @FXML private Label emotionsLabel;
    @FXML private Button btnVoir;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;

    private Reve reve;
    private ReveController parentController;

    public void setData(Reve reve, ReveController parent) {
        this.reve = reve;
        this.parentController = parent;

        afficherDonnees();
        configurerActions();
    }

    private void afficherDonnees() {
        // Titre - COULEUR FORCÉE
        titreLabel.setText(reve.getTitre());
        titreLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Type avec couleur
        typeLabel.setText(reve.getTypeReve());
        typeLabel.setStyle("-fx-font-size: 13px; -fx-padding: 4 10; -fx-background-radius: 5; " +
                getTypeColor(reve.getTypeReve()));

        // Description - COULEUR FORCÉE
        String descCourte = reve.getDescription().length() > 100
                ? reve.getDescription().substring(0, 100) + "..."
                : reve.getDescription();
        descriptionLabel.setText(descCourte);
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        // Humeur - COULEUR FORCÉE
        humeurLabel.setText("Humeur: " +
                (reve.getHumeur() != null ? reve.getHumeur() : "Non specifie"));
        humeurLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");

        // Intensité
        intensiteBar.setProgress(reve.getIntensite() / 10.0);
        intensiteBar.setStyle(getIntensiteStyle(reve.getIntensite()));
        intensiteValue.setText(reve.getIntensite() + "/10");
        intensiteValue.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");

        // Anxiété
        int anxiete = reve.calculerNiveauAnxiete();
        anxieteLabel.setText(String.format("Anxiete: %d/10", anxiete));
        anxieteLabel.setStyle("-fx-font-size: 12px; " + getAnxieteStyle(anxiete));

        // Badges
        couleurBadge.setVisible(reve.isCouleur());
        couleurBadge.setManaged(reve.isCouleur());
        if (reve.isCouleur()) {
            couleurBadge.setStyle("-fx-background-color: #B2EBF2; -fx-text-fill: #0277BD; -fx-padding: 3 8; -fx-background-radius: 3; -fx-font-size: 11px;");
        }

        recurrentBadge.setVisible(reve.isRecurrent());
        recurrentBadge.setManaged(reve.isRecurrent());
        if (reve.isRecurrent()) {
            recurrentBadge.setStyle("-fx-background-color: #FFCCBC; -fx-text-fill: #E64A19; -fx-padding: 3 8; -fx-background-radius: 3; -fx-font-size: 11px;");
        }

        lucideBadge.setVisible(reve.estLucide());
        lucideBadge.setManaged(reve.estLucide());
        if (reve.estLucide()) {
            lucideBadge.setStyle("-fx-background-color: #C5E1A5; -fx-text-fill: #2E7D32; -fx-padding: 3 8; -fx-background-radius: 3; -fx-font-size: 11px;");
        }

        // Émotions
        if (reve.getEmotions() != null && !reve.getEmotions().isEmpty()) {
            emotionsLabel.setText("Emotions: " + reve.getEmotions());
            emotionsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666; -fx-font-style: italic;");
            emotionsLabel.setVisible(true);
            emotionsLabel.setManaged(true);
        }
    }

    private void configurerActions() {
        btnVoir.setOnAction(e -> parentController.voirDetailsPublic(reve));
        btnModifier.setOnAction(e -> parentController.modifierRevePublic(reve));
        btnSupprimer.setOnAction(e -> parentController.supprimerRevePublic(reve));
    }

    private String getTypeColor(String type) {
        switch (type.toLowerCase()) {
            case "cauchemar":
                return "-fx-background-color: #f44336; -fx-text-fill: white;";
            case "lucide":
                return "-fx-background-color: #4CAF50; -fx-text-fill: white;";
            case "recurrent":
                return "-fx-background-color: #FF9800; -fx-text-fill: white;";
            default:
                return "-fx-background-color: #26C6DA; -fx-text-fill: white;";
        }
    }

    private String getIntensiteStyle(int intensite) {
        if (intensite >= 8) {
            return "-fx-accent: #f44336;";
        } else if (intensite >= 6) {
            return "-fx-accent: #FF9800;";
        } else {
            return "-fx-accent: #4CAF50;";
        }
    }

    private String getAnxieteStyle(int anxiete) {
        if (anxiete >= 7) {
            return "-fx-text-fill: #f44336; -fx-font-weight: bold;";
        } else if (anxiete >= 4) {
            return "-fx-text-fill: #FF9800;";
        } else {
            return "-fx-text-fill: #4CAF50;";
        }
    }
}
