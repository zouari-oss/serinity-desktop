package com.serinity.accesscontrol.controller.sleepcontrol;

import com.serinity.accesscontrol.model.sleepcontrol.Reve;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class ReveCardController {

    @FXML private VBox cardRoot;
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
    @FXML private Button btnAnalyse;

    private Reve reve;
    private ReveController parentController;

    public void setData(Reve reve, ReveController parent) {
        this.reve = reve;
        this.parentController = parent;
        appliquerStylesBoutons();
        afficherDonnees();
        configurerActions();
    }

    /* ── Styles + textes courts lisibles ── */
    private void appliquerStylesBoutons() {
        // Nettoyer tous les anciens styles
        String[] oldStyles = {
                "btn-primary", "btn-secondary", "btn-warning",
                "btn-danger",  "btn-success",   "btn-light"
        };
        btnVoir.getStyleClass().removeAll(oldStyles);
        btnModifier.getStyleClass().removeAll(oldStyles);
        btnSupprimer.getStyleClass().removeAll(oldStyles);
        btnAnalyse.getStyleClass().removeAll(oldStyles);

        // Nouveaux styles premium
        btnVoir.getStyleClass().addAll("btn-card-voir", "btn-sm");
        btnAnalyse.getStyleClass().addAll("btn-secondary", "btn-sm");
        btnModifier.getStyleClass().addAll("btn-card-modifier", "btn-sm");
        btnSupprimer.getStyleClass().addAll("btn-card-delete", "btn-sm");

        // Textes courts sans emoji → lisibles dans 280px
        btnVoir.setText("Voir");
        btnAnalyse.setText("IA");
        btnModifier.setText("Modifier");
        btnSupprimer.setText("X");

        // Largeurs fixes pour éviter la troncature
        btnVoir.setPrefWidth(52);
        btnAnalyse.setPrefWidth(38);
        btnModifier.setPrefWidth(76);
        btnSupprimer.setPrefWidth(32);
    }

    private void afficherDonnees() {

        // ── Titre ──
        titreLabel.setText(reve.getTitre());
        if (!titreLabel.getStyleClass().contains("card-date"))
            titreLabel.getStyleClass().add("card-date");

        // ── Type badge ──
        typeLabel.setText(reve.getTypeReve());
        typeLabel.getStyleClass().removeAll(
                "type-cauchemar", "type-lucide", "type-recurrent", "type-normal"
        );
        if (!typeLabel.getStyleClass().contains("type-label"))
            typeLabel.getStyleClass().add("type-label");
        typeLabel.getStyleClass().add(getTypeStyleClass(reve.getTypeReve()));

        // ── Description ──
        String descCourte = (reve.getDescription() != null && reve.getDescription().length() > 90)
                ? reve.getDescription().substring(0, 90) + "…"
                : (reve.getDescription() != null ? reve.getDescription() : "");
        descriptionLabel.setText(descCourte);
        if (!descriptionLabel.getStyleClass().contains("card-info"))
            descriptionLabel.getStyleClass().add("card-info");

        // ── Humeur ──
        humeurLabel.setText("Humeur: " +
                (reve.getHumeur() != null ? reve.getHumeur() : "Non spécifiée"));
        if (!humeurLabel.getStyleClass().contains("card-info"))
            humeurLabel.getStyleClass().add("card-info");

        // ── Intensité ──
        intensiteBar.setProgress(reve.getIntensite() / 10.0);
        intensiteBar.getStyleClass().removeAll(
                "intensite-haute", "intensite-moyenne", "intensite-basse"
        );
        if      (reve.getIntensite() >= 8) intensiteBar.getStyleClass().add("intensite-haute");
        else if (reve.getIntensite() >= 6) intensiteBar.getStyleClass().add("intensite-moyenne");
        else                               intensiteBar.getStyleClass().add("intensite-basse");

        intensiteValue.setText(reve.getIntensite() + "/10");
        if (!intensiteValue.getStyleClass().contains("card-info-secondary"))
            intensiteValue.getStyleClass().add("card-info-secondary");

        // ── Anxiété ──
        int anxiete = reve.calculerNiveauAnxiete();
        anxieteLabel.setText(String.format("Anxiété: %d/10", anxiete));
        anxieteLabel.getStyleClass().removeAll(
                "anxiete-haute", "anxiete-moyenne", "anxiete-basse"
        );
        if      (anxiete >= 7) anxieteLabel.getStyleClass().add("anxiete-haute");
        else if (anxiete >= 4) anxieteLabel.getStyleClass().add("anxiete-moyenne");
        else                   anxieteLabel.getStyleClass().add("anxiete-basse");

        // ── Badges ──
        appliquerBadge(couleurBadge,   reve.isCouleur(),   "badge-couleur",   "🎨 Couleur");
        appliquerBadge(recurrentBadge, reve.isRecurrent(), "badge-recurrent", "🔁 Récurrent");
        appliquerBadge(lucideBadge,    reve.estLucide(),   "badge-lucide",    "✨ Lucide");

        // ── Émotions ──
        if (reve.getEmotions() != null && !reve.getEmotions().isEmpty()) {
            emotionsLabel.setText("💭 " + reve.getEmotions());
            if (!emotionsLabel.getStyleClass().contains("card-dreams"))
                emotionsLabel.getStyleClass().add("card-dreams");
            emotionsLabel.setVisible(true);
            emotionsLabel.setManaged(true);
        } else {
            emotionsLabel.setVisible(false);
            emotionsLabel.setManaged(false);
        }
    }

    /* ── Helper badge ── */
    private void appliquerBadge(Label badge, boolean visible, String styleClass, String texte) {
        badge.setVisible(visible);
        badge.setManaged(visible);
        if (visible) {
            badge.setText(texte);
            badge.getStyleClass().removeAll(
                    "badge-couleur", "badge-recurrent", "badge-lucide",
                    "badge-cauchemar", "badge-normal"
            );
            if (!badge.getStyleClass().contains("badge"))
                badge.getStyleClass().add("badge");
            badge.getStyleClass().add(styleClass);
        }
    }

    private void configurerActions() {
        btnVoir.setOnAction(e      -> parentController.voirDetailsPublic(reve));
        btnModifier.setOnAction(e  -> parentController.modifierRevePublic(reve));
        btnSupprimer.setOnAction(e -> parentController.supprimerRevePublic(reve));
        btnAnalyse.setOnAction(e   -> parentController.analyserRevePublic(reve));
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
