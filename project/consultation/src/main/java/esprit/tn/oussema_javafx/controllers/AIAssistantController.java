package esprit.tn.oussema_javafx.controllers;

import esprit.tn.oussema_javafx.Router;
import esprit.tn.oussema_javafx.services.MedicalAIService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class AIAssistantController {

    @FXML private TextArea symptomArea;
    @FXML private Label urgencyLabel;
    @FXML private Label emotionLabel;
    @FXML private TextArea recommendationArea;

    @FXML
    public void analyze() {

        String text = symptomArea.getText();

        if (text == null || text.isBlank()) {
            urgencyLabel.setText("Veuillez entrer un symptôme.");
            urgencyLabel.setStyle("-fx-text-fill: #cc0000; -fx-font-weight: bold;");
            return;
        }

        urgencyLabel.setText("Analyse en cours...");
        urgencyLabel.setStyle("-fx-text-fill: #555; -fx-font-weight: bold;");
        emotionLabel.setText("");
        recommendationArea.setText("");

        new Thread(() -> {

            MedicalAIService.AIResult result = MedicalAIService.predict(text);

            Platform.runLater(() -> {
                urgencyLabel.setText(result.urgency);
                emotionLabel.setText(result.emotion);

                // ✅ on affiche la recommendation de l’API (pas une simple map locale)
                if (result.recommendation != null && !result.recommendation.isBlank()) {
                    recommendationArea.setText(result.recommendation);
                } else {
                    recommendationArea.setText(generateAdviceFallback(result.urgency));
                }

                applyUrgencyStyle(result.urgency);
            });

        }).start();
    }

    private void applyUrgencyStyle(String urgency) {
        if (urgency == null) urgency = "UNKNOWN";

        switch (urgency) {
            case "HIGH":
                urgencyLabel.setStyle("-fx-text-fill: #d90429; -fx-font-weight: bold; -fx-font-size: 16px;");
                break;
            case "MEDIUM":
                urgencyLabel.setStyle("-fx-text-fill: #f77f00; -fx-font-weight: bold; -fx-font-size: 16px;");
                break;
            case "LOW":
                urgencyLabel.setStyle("-fx-text-fill: #2a9d8f; -fx-font-weight: bold; -fx-font-size: 16px;");
                break;
            default:
                urgencyLabel.setStyle("-fx-text-fill: #555; -fx-font-weight: bold; -fx-font-size: 16px;");
        }
    }

    private String generateAdviceFallback(String urgency) {
        switch (urgency) {
            case "HIGH":
                return "⚠ Situation potentiellement urgente.\nVeuillez consulter un médecin immédiatement ou appeler les urgences.";
            case "MEDIUM":
                return "Consultez un médecin dans les prochaines 24h.";
            default:
                return "Symptômes légers. Repos recommandé et surveillance.";
        }
    }

    @FXML
    public void back(){
        Router.go("/fxml/doctor/doctor_list.fxml","Mes RDV");
    }
}