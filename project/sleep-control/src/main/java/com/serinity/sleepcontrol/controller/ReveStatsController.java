package com.serinity.sleepcontrol.controller;

import com.serinity.sleepcontrol.service.ReveService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.Map;

public class ReveStatsController {

    // ─── KPIs ────────────────────────────────────────────────────────────────────

    @FXML private Label kpiTotal;
    @FXML private Label kpiIntensite;
    @FXML private Label kpiAnxiete;
    @FXML private Label kpiBienEtre;
    @FXML private Label kpiResilience;

    // ─── Barres % ─────────────────────────────────────────────────────────────────

    @FXML private ProgressBar barCauchemars;
    @FXML private Label       pctCauchemars;
    @FXML private ProgressBar barRecurrents;
    @FXML private Label       pctRecurrents;
    @FXML private ProgressBar barCouleur;
    @FXML private Label       pctCouleur;

    // ─── Conteneurs dynamiques ────────────────────────────────────────────────────

    @FXML private VBox typesContainer;
    @FXML private VBox emotionsContainer;
    @FXML private VBox symbolesContainer;
    @FXML private VBox insightsContainer;

    // ─── Service ─────────────────────────────────────────────────────────────────

    private ReveService reveService;

    // ─── Init ────────────────────────────────────────────────────────────────────

    public void setReveService(ReveService service) {
        this.reveService = service;
        chargerStats();
    }

    @FXML
    private void actualiser() {
        chargerStats();
    }

    // ─── Chargement ──────────────────────────────────────────────────────────────

    private void chargerStats() {
        try {
            chargerKpis();
            chargerPourcentages();
            chargerRepartitionTypes();
            chargerTopEmotions();
            chargerTopSymboles();
            chargerInsights();
        } catch (SQLException e) {
            kpiTotal.setText("Erreur de chargement");
            e.printStackTrace();
        }
    }

    private void chargerKpis() throws SQLException {
        int    total      = reveService.compterTotal();
        double intensite  = reveService.calculerIntensiteMoyenne();
        double anxiete    = reveService.calculerAnxieteMoyenne();
        int    bienEtre   = reveService.calculerScoreBienEtreOnirique();
        double resilience = reveService.calculerIndexResilience();

        kpiTotal.setText(total + " rêves");
        kpiIntensite.setText(String.format("%.1f / 10", intensite));
        kpiAnxiete.setText(String.format("%.1f / 10", anxiete));
        kpiBienEtre.setText(bienEtre + " / 100");
        kpiResilience.setText(String.format("%.0f%%  %s",
                resilience * 100,
                reveService.libelleResilience(resilience)));
    }

    private void chargerPourcentages() throws SQLException {
        double pctC = reveService.calculerPourcentageCauchemars();
        double pctR = reveService.calculerPourcentageRecurrents();
        double pctCo = reveService.statistiquesGlobales()
                .containsKey("pourcentageCouleur")
                ? (double) reveService.statistiquesGlobales().get("pourcentageCouleur")
                : 0;

        setBar(barCauchemars, pctCauchemars, pctC);
        setBar(barRecurrents, pctRecurrents, pctR);
        setBar(barCouleur,    pctCouleur,    pctCo);
    }

    private void chargerRepartitionTypes() throws SQLException {
        typesContainer.getChildren().clear();
        Map<String, Long> types = reveService.compterParType();
        int total = reveService.compterTotal();

        types.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> {
                    double pct = total > 0 ? (e.getValue() * 100.0 / total) : 0;
                    typesContainer.getChildren().add(
                            ligneStats(e.getKey(), e.getValue(), pct)
                    );
                });
    }

    private void chargerTopEmotions() throws SQLException {
        emotionsContainer.getChildren().clear();
        reveService.emotionsFrequentes().forEach((emotion, count) ->
                emotionsContainer.getChildren().add(
                        lignePuces(emotion, count + " fois")
                ));
    }

    private void chargerTopSymboles() throws SQLException {
        symbolesContainer.getChildren().clear();
        reveService.symbolesFrequents().forEach((symbole, count) ->
                symbolesContainer.getChildren().add(
                        lignePuces(symbole, count + " fois")
                ));
    }

    private void chargerInsights() throws SQLException {
        insightsContainer.getChildren().clear();
        reveService.obtenirInsights().forEach(insight -> {
            Label l = new Label(insight);
            l.getStyleClass().add("card-info");
            l.setWrapText(true);
            insightsContainer.getChildren().add(l);
        });
    }

    // ─── Utilitaires UI ──────────────────────────────────────────────────────────

    /** Ligne : label + barre + count + % */
    private HBox ligneStats(String label, long count, double pct) {
        Label lbl  = new Label(String.format("%-12s", label));
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

    /** Ligne simple : • label — valeur */
    private Label lignePuces(String label, String valeur) {
        Label l = new Label("• " + label + "  —  " + valeur);
        l.getStyleClass().add("card-info");
        return l;
    }

    /** Met à jour une barre de progression + son label %. */
    private void setBar(ProgressBar bar, Label lbl, double pct) {
        bar.setProgress(pct / 100.0);
        lbl.setText(String.format("%.0f%%", pct));
        bar.getStyleClass().removeAll("intensite-basse", "intensite-moyenne", "intensite-haute");
        bar.getStyleClass().add(
                pct >= 50 ? "intensite-haute"
                        : pct >= 25 ? "intensite-moyenne"
                        : "intensite-basse"
        );
    }
}
