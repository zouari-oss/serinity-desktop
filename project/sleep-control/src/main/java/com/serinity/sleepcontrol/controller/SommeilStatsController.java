package com.serinity.sleepcontrol.controller;

import com.serinity.sleepcontrol.service.SommeilService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
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

    @FXML private VBox      qualiteContainer;
    @FXML private VBox      humeurContainer;
    @FXML private VBox      tendancesContainer;
    @FXML private FlowPane  insightsContainer;  // ← FlowPane pour les chips

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
        kpiEfficacite.setText(String.format("%.0f %%", efficacite));

        // Dette : couleur rouge si sévère
        String libelleDette = sommeilService.libelleDette(dette);
        kpiDette.setText(libelleDette);
        kpiDette.getStyleClass().remove("dette-critique");
        if (libelleDette.toLowerCase().contains("sévère")
                || libelleDette.toLowerCase().contains("critique")) {
            kpiDette.getStyleClass().add("dette-critique");
        }

        // Score : couleur selon niveau
        kpiScore.getStyleClass().removeAll("score-excellent", "score-moyen", "score-faible");
        if      (scoreMoy >= 70) kpiScore.getStyleClass().add("score-excellent");
        else if (scoreMoy >= 45) kpiScore.getStyleClass().add("score-moyen");
        else                     kpiScore.getStyleClass().add("score-faible");
    }

    // ─── Bien-être & Résilience ───────────────────────────────────────────────────

    private void chargerBienEtre() throws SQLException {
        int    scoreBE    = sommeilService.calculerScoreBienEtre();
        double resilience = sommeilService.calculerIndexResilience();
        double pctOpt     = sommeilService.calculerPourcentageDureeOptimale();

        setBar(barBienEtre,   lblBienEtre,   scoreBE / 100.0,
                String.format("%d/100 — %s", scoreBE,
                        sommeilService.libelleScore(scoreBE)));

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
            Label l = new Label("▸ "
                    + cle.substring(0, 1).toUpperCase() + cle.substring(1)
                    + " : " + valeur);
            l.getStyleClass().add("tendance-label");
            tendancesContainer.getChildren().add(l);
        });

        // Régularité
        double regularite = sommeilService.calculerRegulariteHoraires();
        Label lblReg = new Label("▸ Régularité : "
                + sommeilService.libelleRegularite(regularite));
        lblReg.getStyleClass().add("tendance-label");
        tendancesContainer.getChildren().add(lblReg);

        // Profil chronobiologique
        Label lblProfil = new Label("▸ " + sommeilService.determinerProfilChronobiologique());
        lblProfil.getStyleClass().add("tendance-label");
        tendancesContainer.getChildren().add(lblProfil);
    }

    // ─── Insights (chips colorés dans FlowPane) ───────────────────────────────────

    private void chargerInsights() throws SQLException {
        insightsContainer.getChildren().clear();

        sommeilService.obtenirInsights().forEach(insight -> {
            String lower     = insight.toLowerCase();
            String emoji     = "💡";
            String chipStyle = "insight-chip-neutral";

            if (lower.contains("anxiété")   || lower.contains("dette")
                    || lower.contains("préoccupant")|| lower.contains("sévère")
                    || lower.contains("critique")  || lower.contains("insuffisant")) {
                emoji     = "⚠️";
                chipStyle = "insight-chip-strong";

            } else if (lower.contains("excellent") || lower.contains("optimal")
                    || lower.contains("bien-être")  || lower.contains("équilibré")
                    || lower.contains("bon")        || lower.contains("régulier")) {
                emoji     = "✅";
                chipStyle = "insight-chip-good";
            }

            Label chip = new Label(emoji + "  " + insight);
            chip.getStyleClass().addAll("insight-chip", chipStyle);
            chip.setWrapText(true);
            chip.setMaxWidth(320);

            insightsContainer.getChildren().add(chip);
        });

        // Message si aucun insight
        if (insightsContainer.getChildren().isEmpty()) {
            Label empty = new Label("💡  Enregistrez plus de nuits pour obtenir des insights personnalisés.");
            empty.getStyleClass().addAll("insight-chip", "insight-chip-neutral");
            empty.setWrapText(true);
            insightsContainer.getChildren().add(empty);
        }
    }

    // ─── Utilitaires UI ──────────────────────────────────────────────────────────

    /**
     * Ligne avec label + barre de progression colorée + valeur (répartition qualité)
     */
    private HBox ligneStats(String label, long count, double pct) {
        // Badge qualité coloré
        Label lbl = new Label(label);
        lbl.setPrefWidth(95);
        lbl.setWrapText(false);
        // Appliquer la couleur selon la qualité
        String badgeStyle = switch (label.toLowerCase()) {
            case "excellente" -> "qualite-excellente";
            case "bonne"      -> "qualite-bonne";
            case "moyenne"    -> "qualite-moyenne";
            case "mauvaise"   -> "qualite-mauvaise";
            default           -> "tendance-label";
        };
        lbl.getStyleClass().addAll("type-label", badgeStyle);

        // Barre de progression
        ProgressBar bar = new ProgressBar(pct / 100.0);
        bar.setPrefWidth(120);
        bar.setPrefHeight(8);
        String barStyle = pct >= 60 ? "intensite-haute"
                : pct >= 30 ? "intensite-moyenne"
                : "intensite-basse";
        bar.getStyleClass().add(barStyle);

        // Valeur
        Label val = new Label(count + "  (" + String.format("%.0f%%", pct) + ")");
        val.getStyleClass().add("repartition-value");

        HBox row = new HBox(8, lbl, bar, val);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return row;
    }

    /**
     * Ligne avec puce pour la répartition humeur
     */
    private Label lignePuces(String label, String valeur) {
        String emoji = switch (label.toLowerCase()) {
            case "reposé"    -> "😊";
            case "fatigué"   -> "😴";
            case "anxieux"   -> "😰";
            case "neutre"    -> "😐";
            case "heureux"   -> "😁";
            case "stressé"   -> "😟";
            default          -> "•";
        };
        Label l = new Label(emoji + "  " + label + "   " + valeur);
        l.getStyleClass().add("tendance-label");
        return l;
    }

    /**
     * Met à jour une barre de progression avec son label et sa classe de couleur
     */
    private void setBar(ProgressBar bar, Label lbl, double progress, String texte) {
        bar.setProgress(Math.max(0, Math.min(1, progress)));
        lbl.setText(texte);
        lbl.getStyleClass().add("stat-bar-label");

        bar.getStyleClass().removeAll("intensite-basse", "intensite-moyenne", "intensite-haute");
        bar.getStyleClass().add(
                progress >= 0.70 ? "intensite-haute"
                        : progress >= 0.40 ? "intensite-moyenne"
                        : "intensite-basse"
        );
    }
}
