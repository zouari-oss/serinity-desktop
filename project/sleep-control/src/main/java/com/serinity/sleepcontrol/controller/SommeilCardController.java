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

        qualiteLabel.setText(sommeil.getQualite());
        qualiteLabel.getStyleClass().removeAll(
                "qualite-excellente", "qualite-bonne", "qualite-moyenne", "qualite-mauvaise"
        );
        qualiteLabel.getStyleClass().add(getQualiteStyleClass(sommeil.getQualite()));

        heuresLabel.setText(String.format("Heures: %s -> %s",
                sommeil.getHeureCoucher(), sommeil.getHeureReveil()));

        dureeLabel.setText(String.format("Duree: %.2f heures",
                sommeil.getDureeSommeil()));

        interruptionsLabel.setText(String.format("Interruptions: %d",
                sommeil.getInterruptions()));

        humeurLabel.setText("Humeur: " +
                (sommeil.getHumeurReveil() != null ? sommeil.getHumeurReveil() : "Non specifie"));

        revesLabel.setText(String.format("%d reve(s)", sommeil.getNbReves()));

        int score = sommeil.calculerScoreQualite();
        scoreLabel.setText("Score: " + score + "/100");
        scoreLabel.getStyleClass().removeAll("score-excellent", "score-moyen", "score-faible");
        scoreLabel.getStyleClass().add(getScoreStyleClass(score));
    }

    private void configurerActions() {
        btnVoir.setOnAction(e -> parentController.voirDetailsPublic(sommeil));
        btnModifier.setOnAction(e -> parentController.modifierSommeilPublic(sommeil));
        btnSupprimer.setOnAction(e -> parentController.supprimerSommeilPublic(sommeil));
    }

    private String getQualiteStyleClass(String qualite) {
        switch (qualite.toLowerCase()) {
            case "excellente": return "qualite-excellente";
            case "bonne":      return "qualite-bonne";
            case "moyenne":    return "qualite-moyenne";
            case "mauvaise":   return "qualite-mauvaise";
            default:           return "qualite-moyenne";
        }
    }

    private String getScoreStyleClass(int score) {
        if      (score >= 80) return "score-excellent";
        else if (score >= 60) return "score-moyen";
        else                  return "score-faible";
    }
}
