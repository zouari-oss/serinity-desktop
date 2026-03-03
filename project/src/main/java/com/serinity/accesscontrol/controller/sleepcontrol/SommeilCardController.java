package com.serinity.accesscontrol.controller.sleepcontrol;

import com.serinity.accesscontrol.model.sleepcontrol.Sommeil;
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
        appliquerStylesBoutons();
        afficherDonnees();
        configurerActions();
    }

    /* ── Styles premium sur les boutons ── */
    private void appliquerStylesBoutons() {
        btnVoir.getStyleClass().removeAll(
                "btn-primary", "btn-secondary", "btn-warning",
                "btn-danger", "btn-success", "btn-light"
        );
        btnModifier.getStyleClass().removeAll(
                "btn-primary", "btn-secondary", "btn-warning",
                "btn-danger", "btn-success", "btn-light"
        );
        btnSupprimer.getStyleClass().removeAll(
                "btn-primary", "btn-secondary", "btn-warning",
                "btn-danger", "btn-success", "btn-light"
        );

        btnVoir.getStyleClass().addAll("btn-card-voir", "btn-sm");
        btnModifier.getStyleClass().addAll("btn-card-modifier", "btn-sm");
        btnSupprimer.getStyleClass().addAll("btn-card-delete", "btn-sm");

        // Texte explicite avec emoji
        btnVoir.setText("👁 Voir");
        btnModifier.setText("✏ Modifier");
        btnSupprimer.setText("✕");
    }

    private void afficherDonnees() {

        // ── Date ──
        dateLabel.setText(
                sommeil.getDateNuit().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );
        dateLabel.getStyleClass().add("card-date");

        // ── Badge qualité ──
        qualiteLabel.setText(sommeil.getQualite());
        qualiteLabel.getStyleClass().removeAll(
                "qualite-excellente", "qualite-bonne",
                "qualite-moyenne", "qualite-mauvaise"
        );
        qualiteLabel.getStyleClass().add(getQualiteStyleClass(sommeil.getQualite()));

        // ── Heures ──
        heuresLabel.setText(String.format("🕙 %s → %s",
                sommeil.getHeureCoucher(), sommeil.getHeureReveil()));
        heuresLabel.getStyleClass().add("card-info");

        // ── Durée ──
        dureeLabel.setText(String.format("⏱ %.2f heures", sommeil.getDureeSommeil()));
        dureeLabel.getStyleClass().add("card-info");

        // ── Interruptions ──
        interruptionsLabel.setText(String.format("🔔 Interruptions: %d",
                sommeil.getInterruptions()));
        interruptionsLabel.getStyleClass().add("card-info");

        // ── Humeur ──
        String humeurTxt = sommeil.getHumeurReveil() != null
                ? sommeil.getHumeurReveil() : "Non spécifié";
        humeurLabel.setText("😴 Humeur: " + humeurTxt);
        humeurLabel.getStyleClass().add("card-info");

        // ── Rêves ──
        int nbReves = sommeil.getNbReves();
        revesLabel.setText(nbReves == 0
                ? "💤 Aucun rêve"
                : String.format("🌙 %d rêve(s)", nbReves));
        revesLabel.getStyleClass().add("card-dreams");

        // ── Score ──
        int score = sommeil.calculerScoreQualite();
        scoreLabel.setText("Score: " + score + "/100");
        scoreLabel.getStyleClass().removeAll(
                "score-excellent", "score-moyen", "score-faible"
        );
        scoreLabel.getStyleClass().addAll("score-label", getScoreStyleClass(score));
    }

    private void configurerActions() {
        btnVoir.setOnAction(e      -> parentController.voirDetailsPublic(sommeil));
        btnModifier.setOnAction(e  -> parentController.modifierSommeilPublic(sommeil));
        btnSupprimer.setOnAction(e -> parentController.supprimerSommeilPublic(sommeil));
    }

    private String getQualiteStyleClass(String qualite) {
        if (qualite == null) return "qualite-moyenne";
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
