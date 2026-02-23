package com.serinity.sleepcontrol.controller;

import com.serinity.sleepcontrol.service.ReveService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.Map;

public class ReveStatsController {

    @FXML private Label kpiTotal;
    @FXML private Label kpiIntensite;
    @FXML private Label kpiAnxiete;
    @FXML private Label kpiBienEtre;
    @FXML private Label kpiResilience;

    @FXML private ProgressBar barCauchemars;
    @FXML private Label       pctCauchemars;
    @FXML private ProgressBar barRecurrents;
    @FXML private Label       pctRecurrents;
    @FXML private ProgressBar barCouleur;
    @FXML private Label       pctCouleur;

    @FXML private VBox      typesContainer;
    @FXML private VBox      emotionsContainer;
    @FXML private VBox      symbolesContainer;
    @FXML private FlowPane  insightsContainer;

    private ReveService reveService;

    @FXML
    public void initialize() {
        if (reveService != null) chargerStats();
    }

    public void setReveService(ReveService service) {
        this.reveService = service;
        Platform.runLater(this::chargerStats);
    }

    @FXML
    private void actualiser() {
        chargerStats();
    }

    private void chargerStats() {
        try {
            chargerKpis();
            chargerPourcentages();
            chargerRepartitionTypes();
            chargerTopEmotions();
            chargerTopSymboles();
            chargerInsights();
        } catch (SQLException e) {
            kpiTotal.setText("Erreur");
            e.printStackTrace();
        }
    }

    private void chargerKpis() throws SQLException {
        int    total      = reveService.compterTotal();
        double intensite  = reveService.calculerIntensiteMoyenne();
        double anxiete    = reveService.calculerAnxieteMoyenne();
        int    bienEtre   = reveService.calculerScoreBienEtreOnirique();
        double resilience = reveService.calculerIndexResilience();

        kpiTotal.setText(String.valueOf(total));
        kpiIntensite.setText(String.format("%.1f / 10", intensite));
        kpiAnxiete.setText(String.format("%.1f / 10", anxiete));
        kpiBienEtre.setText(bienEtre + " / 100");
        kpiResilience.setText(String.format("%.0f%%", resilience * 100));

        kpiBienEtre.getStyleClass().removeAll("score-excellent", "score-moyen", "score-faible");
        if      (bienEtre >= 70) kpiBienEtre.getStyleClass().add("score-excellent");
        else if (bienEtre >= 45) kpiBienEtre.getStyleClass().add("score-moyen");
        else                     kpiBienEtre.getStyleClass().add("score-faible");

        kpiAnxiete.getStyleClass().removeAll("anxiete-haute", "anxiete-moyenne", "anxiete-basse");
        if      (anxiete >= 7) kpiAnxiete.getStyleClass().add("anxiete-haute");
        else if (anxiete >= 4) kpiAnxiete.getStyleClass().add("anxiete-moyenne");
        else                   kpiAnxiete.getStyleClass().add("anxiete-basse");
    }

    private void chargerPourcentages() throws SQLException {
        double pctC  = reveService.calculerPourcentageCauchemars();
        double pctR  = reveService.calculerPourcentageRecurrents();
        double pctCo = reveService.statistiquesGlobales().containsKey("pourcentageCouleur")
                ? (double) reveService.statistiquesGlobales().get("pourcentageCouleur") : 0;

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
                    typesContainer.getChildren().add(ligneStats(e.getKey(), e.getValue(), pct));
                });
    }

    private void chargerTopEmotions() throws SQLException {
        emotionsContainer.getChildren().clear();
        reveService.emotionsFrequentes().forEach((emotion, count) ->
                emotionsContainer.getChildren().add(lignePuces(emotion, count + " fois")));
    }

    private void chargerTopSymboles() throws SQLException {
        symbolesContainer.getChildren().clear();
        reveService.symbolesFrequents().forEach((symbole, count) ->
                symbolesContainer.getChildren().add(lignePuces(symbole, count + " fois")));
    }

    private void chargerInsights() throws SQLException {
        insightsContainer.getChildren().clear();

        reveService.obtenirInsights().forEach(insight -> {
            String lower     = insight.toLowerCase();
            String emoji     = "💡";
            String chipStyle = "insight-chip-neutral";

            if (lower.contains("cauchemar") || lower.contains("anxiété")
                    || lower.contains("élevé") || lower.contains("critique")) {
                emoji     = "⚠️";
                chipStyle = "insight-chip-strong";
            } else if (lower.contains("excellent") || lower.contains("bon")
                    || lower.contains("lucide")    || lower.contains("positif")) {
                emoji     = "✅";
                chipStyle = "insight-chip-good";
            }

            Label chip = new Label(emoji + "  " + insight);
            chip.getStyleClass().addAll("insight-chip", chipStyle);
            chip.setWrapText(true);
            chip.setMaxWidth(320);
            insightsContainer.getChildren().add(chip);
        });

        if (insightsContainer.getChildren().isEmpty()) {
            Label empty = new Label("💡  Enregistrez plus de rêves pour obtenir des insights.");
            empty.getStyleClass().addAll("insight-chip", "insight-chip-neutral");
            empty.setWrapText(true);
            insightsContainer.getChildren().add(empty);
        }
    }

    private HBox ligneStats(String label, long count, double pct) {
        Label lbl = new Label(label);
        lbl.setPrefWidth(95);
        lbl.setWrapText(false);
        String badgeStyle = switch (label.toLowerCase()) {
            case "cauchemar"  -> "type-cauchemar";
            case "lucide"     -> "type-lucide";
            case "recurrent"  -> "type-recurrent";
            default           -> "type-normal";
        };
        lbl.getStyleClass().addAll("type-label", badgeStyle);

        ProgressBar bar = new ProgressBar(pct / 100.0);
        bar.setPrefWidth(120);
        bar.setPrefHeight(8);
        bar.getStyleClass().add(pct >= 60 ? "intensite-haute"
                : pct >= 30 ? "intensite-moyenne" : "intensite-basse");

        Label val = new Label(count + "  (" + String.format("%.0f%%", pct) + ")");
        val.getStyleClass().add("repartition-value");

        HBox row = new HBox(8, lbl, bar, val);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return row;
    }

    private Label lignePuces(String label, String valeur) {
        Label l = new Label("▸  " + label + "   " + valeur);
        l.getStyleClass().add("tendance-label");
        return l;
    }

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
