package com.serinity.sleepcontrol.controller;

import com.serinity.sleepcontrol.service.SommeilService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;

public class SommeilStatsController {

    // ─── KPIs ────────────────────────────────────────────────────────────────────

    @FXML private Label kpiTotal;
    @FXML private Label kpiDuree;
    @FXML private Label kpiScore;
    @FXML private Label kpiDette;
    @FXML private Label kpiEfficacite;

    // ─── Barres bien-être ─────────────────────────────────────────────────────────

    @FXML private ProgressBar barBienEtre;
    @FXML private Label       lblBienEtre;
    @FXML private ProgressBar barResilience;
    @FXML private Label       lblResilience;
    @FXML private ProgressBar barOptimale;
    @FXML private Label       lblOptimale;

    // ─── Conteneurs dynamiques ────────────────────────────────────────────────────

    @FXML private VBox qualiteContainer;
    @FXML private VBox humeurContainer;
    @FXML private VBox tendancesContainer;
    @FXML private VBox insightsContainer;

    // ─── Service ─────────────────────────────────────────────────────────────────

    private SommeilService sommeilService;

    // ─── Init ────────────────────────────────────────────────────────────────────

    public void setSommeilService(SommeilService service) {
        this.sommeilService = service;
        chargerStats();
    }

    @FXML
    private void actualiser() {
        chargerStats();
    }

    // ─── Chargement principal ─────────────────────────────────────────────────────

    private void chargerStats() {
        try {
            chargerKpis();
            chargerBienEtre();
            chargerRepartitionQualite();
            chargerRepartitionHumeur();
            chargerTendances();
            chargerInsights();
        } catch (SQLException e) {
            kpiTotal.setText("Erreur");
            e.printStackTrace();
        }
    }

    // ─── KPIs ─────────────────────────────────────────────────────────────────────

    private void chargerKpis() throws SQLException {
        int    total      = sommeilService.compterTotal();
        double dureeMoy   = sommeilService.calculerDureeMoyenne();
        double scoreMoy   = sommeilService.calculerScoreMoyen();
        double dette      = sommeilService.calculerDetteSommeil();
        double efficacite = sommeilService.calculerEfficaciteMoyenne();

        kpiTotal.setText(String.valueOf(total));
        kpiDuree.setText(String.format("%.1f h", dureeMoy));
        kpiScore.setText(String.format("%.0f /100", scoreMoy));
        kpiDette.setText(sommeilService.libelleDette(dette));
        kpiEfficacite.setText(String.format("%.0f %%", efficacite));
    }

    // ─── Bien-être & Résilience ───────────────────────────────────────────────────

    private void chargerBienEtre() throws SQLException {
        int    scoreBE    = sommeilService.calculerScoreBienEtre();
        double resilience = sommeilService.calculerIndexResilience();
        double pctOpt     = sommeilService.calculerPourcentageDureeOptimale();

        setBar(barBienEtre,   lblBienEtre,   scoreBE / 100.0,
                String.format("%d/100 — %s", scoreBE, sommeilService.libelleScore(scoreBE)));
        setBar(barResilience, lblResilience, resilience,
                String.format("%.0f%% — %s", resilience * 100,
                        sommeilService.libelleResilience(resilience)));
        setBar(barOptimale,   lblOptimale,   pctOpt / 100.0,
                String.format("%.0f%%", pctOpt));
    }

    // ─── Répartition qualité ──────────────────────────────────────────────────────

    private void chargerRepartitionQualite() throws SQLException {
        qualiteContainer.getChildren().clear();
        Map<String, Long> qualites = sommeilService.compterParQualite();
        int total = sommeilService.compterTotal();

        qualites.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> {
                    double pct = total > 0 ? (e.getValue() * 100.0 / total) : 0;
                    qualiteContainer.getChildren().add(
                            ligneStats(e.getKey(), e.getValue(), pct));
                });
    }

    // ─── Répartition humeur ───────────────────────────────────────────────────────

    private void chargerRepartitionHumeur() throws SQLException {
        humeurContainer.getChildren().clear();
        sommeilService.compterParHumeur().entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> humeurContainer.getChildren().add(
                        lignePuces(e.getKey(), e.getValue() + " fois")));
    }

    // ─── Tendances (30 derniers jours) ────────────────────────────────────────────

    private void chargerTendances() throws SQLException {
        tendancesContainer.getChildren().clear();
        LocalDate fin   = LocalDate.now();
        LocalDate debut = fin.minusDays(30);

        Map<String, String> tendances = sommeilService.identifierTendances(debut, fin);
        tendances.forEach((cle, valeur) -> {
            Label l = new Label(
                    cle.substring(0, 1).toUpperCase() + cle.substring(1)
                            + " : " + valeur);
            l.getStyleClass().add("card-info");
            tendancesContainer.getChildren().add(l);
        });

        double regularite = sommeilService.calculerRegulariteHoraires();
        Label lblReg = new Label("Régularité : "
                + sommeilService.libelleRegularite(regularite));
        lblReg.getStyleClass().add("card-info");
        tendancesContainer.getChildren().add(lblReg);

        Label lblProfil = new Label(sommeilService.determinerProfilChronobiologique());
        lblProfil.getStyleClass().add("card-info");
        tendancesContainer.getChildren().add(lblProfil);
    }

    // ─── Insights ─────────────────────────────────────────────────────────────────

    private void chargerInsights() throws SQLException {
        insightsContainer.getChildren().clear();
        sommeilService.obtenirInsights().forEach(insight -> {
            Label l = new Label(insight);
            l.getStyleClass().add("card-info");
            l.setWrapText(true);
            insightsContainer.getChildren().add(l);
        });
    }

    // ─── Utilitaires UI ──────────────────────────────────────────────────────────

    private HBox ligneStats(String label, long count, double pct) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("card-info");
        lbl.setPrefWidth(90);

        ProgressBar bar = new ProgressBar(pct / 100.0);
        bar.setPrefWidth(130);
        bar.getStyleClass().add("intensite-moyenne");

        Label val = new Label(count + "  (" + String.format("%.0f%%", pct) + ")");
        val.getStyleClass().add("card-info");

        HBox row = new HBox(8, lbl, bar, val);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return row;
    }

    private Label lignePuces(String label, String valeur) {
        Label l = new Label("• " + label + "  —  " + valeur);
        l.getStyleClass().add("card-info");
        return l;
    }

    private void setBar(ProgressBar bar, Label lbl, double progress, String texte) {
        bar.setProgress(Math.max(0, Math.min(1, progress)));
        lbl.setText(texte);
        bar.getStyleClass().removeAll("intensite-basse", "intensite-moyenne", "intensite-haute");
        bar.getStyleClass().add(
                progress >= 0.70 ? "intensite-haute"
                        : progress >= 0.40 ? "intensite-moyenne"
                        : "intensite-basse"
        );
    }
}
