package com.serinity.sleepcontrol.controller;

import com.serinity.sleepcontrol.model.NlpResultat;
import com.serinity.sleepcontrol.model.Reve;
import com.serinity.sleepcontrol.service.NlpService;
import com.serinity.sleepcontrol.service.ReveService;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

public class HumeurReveilController {

    @FXML private ComboBox<String> comboReve;
    @FXML private Button           btnAnalyser;
    @FXML private VBox             sentimentBox;
    @FXML private Label            lblEmoji;
    @FXML private Label            lblSentiment;
    @FXML private Label            lblConfiance;
    @FXML private ProgressBar      progressBar;
    @FXML private VBox             recoBox;
    @FXML private TextArea         txtRecommandations;

    private ReveService reveService;
    private List<Reve>  reves;

    public void setReveService(ReveService reveService) {
        this.reveService = reveService;
    }

    public void chargerReves() {
        try {
            reves = reveService.listerTous();
            List<String> items = reves.stream()
                    .map(r -> r.getId() + " — " + r.getTitre())
                    .toList();
            comboReve.setItems(FXCollections.observableArrayList(items));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void analyser() {
        String selection = comboReve.getSelectionModel().getSelectedItem();
        if (selection == null) {
            lblEmoji.setText("⚠️");
            lblSentiment.setText("Sélectionne d'abord un rêve !");
            lblSentiment.setStyle("-fx-font-size: 14px; -fx-text-fill: #FF9800;");
            sentimentBox.setVisible(true);
            sentimentBox.setManaged(true);
            return;
        }

        int id = Integer.parseInt(selection.split(" — ")[0].trim());
        Reve reve = reves.stream()
                .filter(r -> r.getId() == id)
                .findFirst().orElse(null);
        if (reve == null) return;

        // Chargement
        btnAnalyser.setDisable(true);
        btnAnalyser.setText("⏳ Analyse en cours...");
        sentimentBox.setVisible(false);
        sentimentBox.setManaged(false);
        recoBox.setVisible(false);
        recoBox.setManaged(false);

        Task<NlpResultat> task = new Task<>() {
            @Override
            protected NlpResultat call() {
                NlpService nlp = new NlpService();

                // Texte pour sentiment
                String texte = "";
                if (reve.getTitre()       != null) texte += reve.getTitre() + ". ";
                if (reve.getDescription() != null) texte += reve.getDescription() + ". ";
                if (reve.getEmotions()    != null) texte += reve.getEmotions();

                // 1. Sentiment
                NlpResultat resultat = nlp.analyser(texte.trim());

                // 2. Recommandations GPT
                String recos = nlp.genererRecommandations(
                        reve.getTitre(),
                        reve.getDescription() != null ? reve.getDescription() : "",
                        reve.getHumeur()      != null ? reve.getHumeur()      : "inconnue",
                        reve.getTypeReve()    != null ? reve.getTypeReve()    : "Normal",
                        resultat.getSentimentFr()
                );
                resultat.setRecommandations(recos);
                return resultat;
            }
        };

        task.setOnSucceeded(e -> {
            NlpResultat nlp = task.getValue();

            lblEmoji.setText(nlp.getEmoji());
            lblSentiment.setText(nlp.getSentimentFr());
            lblSentiment.setStyle(
                    "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: "
                            + nlp.getCouleur() + ";");
            progressBar.setProgress(nlp.getSentimentScore());
            progressBar.setStyle("-fx-accent: " + nlp.getCouleur() + ";");
            lblConfiance.setText(
                    String.format("Confiance : %.0f%%", nlp.getSentimentScore() * 100));

            sentimentBox.setVisible(true);
            sentimentBox.setManaged(true);

            txtRecommandations.setText(nlp.getRecommandations());
            recoBox.setVisible(true);
            recoBox.setManaged(true);

            btnAnalyser.setDisable(false);
            btnAnalyser.setText("🔍 Analyser mon humeur");
        });

        task.setOnFailed(e -> {
            lblEmoji.setText("❌");
            lblSentiment.setText("Erreur de connexion");
            lblSentiment.setStyle("-fx-font-size: 14px; -fx-text-fill: #F44336;");
            lblConfiance.setText(task.getException().getMessage());
            sentimentBox.setVisible(true);
            sentimentBox.setManaged(true);
            btnAnalyser.setDisable(false);
            btnAnalyser.setText("🔍 Analyser mon humeur");
        });

        new Thread(task).start();
    }
}
