package com.serinity.sleepcontrol.controller;

import com.serinity.sleepcontrol.model.Reve;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
        titreLabel.setText(reve.getTitre());

        typeLabel.setText(reve.getTypeReve());
        typeLabel.getStyleClass().removeAll(
                "type-cauchemar", "type-lucide", "type-recurrent", "type-normal"
        );
        typeLabel.getStyleClass().add(getTypeStyleClass(reve.getTypeReve()));

        String descCourte = reve.getDescription() != null && reve.getDescription().length() > 100
                ? reve.getDescription().substring(0, 100) + "..."
                : reve.getDescription() != null ? reve.getDescription() : "";
        descriptionLabel.setText(descCourte);

        humeurLabel.setText("Humeur: " +
                (reve.getHumeur() != null ? reve.getHumeur() : "Non spécifiée"));

        intensiteBar.setProgress(reve.getIntensite() / 10.0);
        intensiteBar.getStyleClass().removeAll("intensite-haute", "intensite-moyenne", "intensite-basse");
        if      (reve.getIntensite() >= 8) intensiteBar.getStyleClass().add("intensite-haute");
        else if (reve.getIntensite() >= 6) intensiteBar.getStyleClass().add("intensite-moyenne");
        else                               intensiteBar.getStyleClass().add("intensite-basse");
        intensiteValue.setText(reve.getIntensite() + "/10");

        int anxiete = reve.calculerNiveauAnxiete();
        anxieteLabel.setText(String.format("Anxiété: %d/10", anxiete));
        anxieteLabel.getStyleClass().removeAll("anxiete-haute", "anxiete-moyenne", "anxiete-basse");
        if      (anxiete >= 7) anxieteLabel.getStyleClass().add("anxiete-haute");
        else if (anxiete >= 4) anxieteLabel.getStyleClass().add("anxiete-moyenne");
        else                   anxieteLabel.getStyleClass().add("anxiete-basse");

        couleurBadge.setVisible(reve.isCouleur());
        couleurBadge.setManaged(reve.isCouleur());

        recurrentBadge.setVisible(reve.isRecurrent());
        recurrentBadge.setManaged(reve.isRecurrent());

        lucideBadge.setVisible(reve.estLucide());
        lucideBadge.setManaged(reve.estLucide());

        if (reve.getEmotions() != null && !reve.getEmotions().isEmpty()) {
            emotionsLabel.setText("Émotions: " + reve.getEmotions());
            emotionsLabel.setVisible(true);
            emotionsLabel.setManaged(true);
        } else {
            emotionsLabel.setVisible(false);
            emotionsLabel.setManaged(false);
        }
    }

    private void configurerActions() {
        btnVoir.setOnAction(e -> parentController.voirDetailsPublic(reve));
        btnModifier.setOnAction(e -> parentController.modifierRevePublic(reve));
        btnSupprimer.setOnAction(e -> parentController.supprimerRevePublic(reve));
    }

    private String getTypeStyleClass(String type) {
        if (type == null) return "type-normal";
        switch (type.toLowerCase()) {
            case "cauchemar": return "type-cauchemar";
            case "lucide":    return "type-lucide";
            case "recurrent": return "type-recurrent";
            default:          return "type-normal";
        }
    }
}
