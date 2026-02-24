package com.serinity.sleepcontrol.controller;

import com.serinity.sleepcontrol.ia.AnalyseResult;
import com.serinity.sleepcontrol.ia.ReveAnalyseIA;
import com.serinity.sleepcontrol.model.Reve;
import com.serinity.sleepcontrol.service.ReveService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;

public class ReveAnalyseController {

    @FXML private Label            lblScore;
    @FXML private Label            lblProfil;
    @FXML private Label            lblImpact;
    @FXML private Label            lblAlerte;
    @FXML private ProgressBar      barScore;
    @FXML private TextArea         txtConclusion;
    @FXML private VBox             symbolesContainer;
    @FXML private Label            lblMode;
    @FXML private ComboBox<String> modeCombo;
    @FXML private FlowPane         recommandationsContainer;

    private ReveService     reveService;
    private Reve            reveUnique;
    private boolean         pret = false;  // ← true quand tout est prêt
    private final ReveAnalyseIA ia = new ReveAnalyseIA();

    @FXML
    public void initialize() {
        modeCombo.getItems().addAll("Analyse globale", "Rêve sélectionné");
        modeCombo.setValue("Analyse globale");
        modeCombo.setOnAction(e -> {
            if (pret) analyser();
        });
    }

    // ─── 1. Toujours appeler EN PREMIER ──────────────────────────────────────────
    public void setReveService(ReveService service) {
        this.reveService = service;
        // Ne lance rien ici — attend demarrerAnalyseGlobale() ou setReveUnique()
    }

    // ─── 2a. Appeler pour une analyse globale ─────────────────────────────────────
    public void demarrerAnalyseGlobale() {
        marquerPret();
    }

    // ─── 2b. Appeler pour analyser un rêve spécifique ────────────────────────────
    public void setReveUnique(Reve reve) {
        this.reveUnique = reve;
        modeCombo.setValue("Rêve sélectionné");
        modeCombo.setDisable(true);
        marquerPret();
    }

    // ─── Déclenche l'analyse quand tout est prêt ─────────────────────────────────
    private void marquerPret() {
        if (reveService == null) return;
        pret = true;
        Platform.runLater(this::analyser);
    }

    @FXML
    private void analyser() {
        if (!pret || reveService == null) return;
        try {
            AnalyseResult result;
            if (reveUnique != null && "Rêve sélectionné".equals(modeCombo.getValue())) {
                result = ia.analyser(reveUnique);
                lblMode.setText("🔍 Analyse : " + reveUnique.getTitre());
            } else {
                List<Reve> tous = reveService.listerTous();
                result = ia.analyserTous(tous);
                lblMode.setText("🔍 Analyse globale — " + tous.size() + " rêves");
            }
            afficherResultat(result);
        } catch (SQLException e) {
            txtConclusion.setText("Erreur lors de l'analyse.");
            e.printStackTrace();
        }
    }

    private void afficherResultat(AnalyseResult result) {
        int score = result.getScorePsychologique();
        lblScore.setText(score + " / 100");
        barScore.setProgress(score / 100.0);
        barScore.getStyleClass().removeAll(
                "intensite-haute", "intensite-moyenne", "intensite-basse");
        barScore.getStyleClass().add(
                score >= 70 ? "intensite-basse" :
                        score >= 40 ? "intensite-moyenne" : "intensite-haute");

        lblProfil.setText("🧠 " + result.getProfilDominant());
        lblImpact.setText(result.getImpactEmotionnel());
        txtConclusion.setText(result.getConclusion());

        lblAlerte.setText(result.getNiveauAlerte());
        lblAlerte.getStyleClass().removeAll(
                "alerte-aucun", "alerte-faible", "alerte-modere",
                "alerte-eleve", "alerte-critique");
        lblAlerte.getStyleClass().add(switch (result.getNiveauAlerte()) {
            case "CRITIQUE" -> "alerte-critique";
            case "ÉLEVÉ"    -> "alerte-eleve";
            case "MODÉRÉ"   -> "alerte-modere";
            case "FAIBLE"   -> "alerte-faible";
            default         -> "alerte-aucun";
        });

        symbolesContainer.getChildren().clear();
        if (result.getSymbolesDetectes() == null || result.getSymbolesDetectes().isEmpty()) {
            Label aucun = new Label("Aucun symbole détecté");
            aucun.getStyleClass().add("tendance-label");
            symbolesContainer.getChildren().add(aucun);
        } else {
            result.getSymbolesDetectes().forEach((sym, desc) -> {
                VBox row = new VBox(2);
                Label lSym = new Label("🔮 " + sym.toUpperCase());
                lSym.getStyleClass().addAll("type-label", "type-normal");
                Label lDesc = new Label(desc);
                lDesc.getStyleClass().add("tendance-label");
                lDesc.setWrapText(true);
                row.getChildren().addAll(lSym, lDesc);
                symbolesContainer.getChildren().add(row);
            });
        }

        recommandationsContainer.getChildren().clear();
        if (result.getRecommandations() != null) {
            result.getRecommandations().forEach(rec -> {
                Label l = new Label(rec);
                l.getStyleClass().addAll("insight-chip", "insight-chip-neutral");
                l.setWrapText(true);
                l.setMaxWidth(500);
                recommandationsContainer.getChildren().add(l);
            });
        }
    }
}
