package com.serinity.sleepcontrol.controller;

import com.serinity.sleepcontrol.model.Sommeil;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;

public class SommeilCardController {

    @FXML private Label dateLabel;
    @FXML private Label qualiteLabel;
    @FXML private Label heuresLabel;
    @FXML private Label dureeLabel;
    @FXML private Label interruptionsLabel;
    @FXML private Label humeurLabel;
    @FXML private Label revesLabel;
    @FXML private Label scoreLabel;
    @FXML private Button btnVoir;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;

    private Sommeil sommeil;
    private SommeilController parentController;

    public void setData(Sommeil sommeil, SommeilController parent) {
        this.sommeil = sommeil;
        this.parentController = parent;

        afficherDonnees();
        configurerActions();
    }

    private void afficherDonnees() {
        dateLabel.setText(
                sommeil.getDateNuit().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );
        dateLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        qualiteLabel.setText(sommeil.getQualite());
        qualiteLabel.setStyle("-fx-font-size: 13px; -fx-padding: 4 10; -fx-background-radius: 5; " +
                getQualiteColor(sommeil.getQualite()));

        heuresLabel.setText(String.format("Heures: %s -> %s",
                sommeil.getHeureCoucher(), sommeil.getHeureReveil()));
        heuresLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");

        dureeLabel.setText(String.format("Duree: %.2f heures",
                sommeil.getDureeSommeil()));
        dureeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");

        interruptionsLabel.setText(String.format("Interruptions: %d",
                sommeil.getInterruptions()));
        interruptionsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");

        humeurLabel.setText("Humeur: " +
                (sommeil.getHumeurReveil() != null ? sommeil.getHumeurReveil() : "Non specifie"));
        humeurLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");

        revesLabel.setText(String.format("%d reve(s)", sommeil.getNombreReves()));
        revesLabel.setStyle("-fx-font-size: 12px; -fx-font-style: italic; -fx-text-fill: #666;");

        int score = sommeil.calculerScoreQualite();
        scoreLabel.setText("Score: " + score + "/100");
        scoreLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; " + getScoreColor(score));
    }

    private void configurerActions() {
        btnVoir.setOnAction(e -> parentController.voirDetailsPublic(sommeil));
        btnModifier.setOnAction(e -> parentController.modifierSommeilPublic(sommeil));
        btnSupprimer.setOnAction(e -> parentController.supprimerSommeilPublic(sommeil));
    }

    private String getQualiteColor(String qualite) {
        switch (qualite.toLowerCase()) {
            case "excellente":
                return "-fx-background-color: #4CAF50; -fx-text-fill: white;";
            case "bonne":
                return "-fx-background-color: #8BC34A; -fx-text-fill: white;";
            case "moyenne":
                return "-fx-background-color: #FF9800; -fx-text-fill: white;";
            case "mauvaise":
                return "-fx-background-color: #f44336; -fx-text-fill: white;";
            default:
                return "-fx-background-color: #9E9E9E; -fx-text-fill: white;";
        }
    }

    private String getScoreColor(int score) {
        if (score >= 80) {
            return "-fx-text-fill: #4CAF50;";
        } else if (score >= 60) {
            return "-fx-text-fill: #FF9800;";
        } else {
            return "-fx-text-fill: #f44336;";
        }
    }
}
