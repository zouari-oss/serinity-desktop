package com.serinity.consultationcontrol.controller;

import com.serinity.consultationcontrol.service.MedicalAIService;
import com.serinity.consultationcontrol.util.Router;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class AIAssistantController {

    @FXML private TextArea symptomArea;
    @FXML private Label urgencyLabel;
    @FXML private Label emotionLabel;
    @FXML private TextArea recommendationArea;
    @FXML private Label assistantStatusLabel;
    @FXML private Label assistantEndpointLabel;

    @FXML
    public void initialize() {
        assistantEndpointLabel.setText(MedicalAIService.preferredEndpointSummary());
        updateServerStatus();
        recommendationArea.setText("Describe the symptoms to get an urgency level, detected emotional tone, and next-step guidance.");
    }

    @FXML
    public void analyze() {
        String text = symptomArea.getText();

        if (text == null || text.isBlank()) {
            urgencyLabel.setText("Waiting for input");
            urgencyLabel.getStyleClass().removeAll("urgency-high", "urgency-medium", "urgency-low");
            emotionLabel.setText("Add a symptom description first.");
            recommendationArea.setText("Example: chest pain, shortness of breath, dizziness, fever, vomiting, or intense stress.");
            assistantStatusLabel.setText("Enter symptoms before running the analysis.");
            assistantStatusLabel.getStyleClass().setAll("assistant-status", "assistant-status-warning");
            return;
        }

        urgencyLabel.setText("Analyzing...");
        urgencyLabel.getStyleClass().removeAll("urgency-high", "urgency-medium", "urgency-low");
        emotionLabel.setText("Processing your description");
        recommendationArea.setText("The medical assistant is reviewing the symptom summary.");
        assistantStatusLabel.setText("Contacting the local AI server...");
        assistantStatusLabel.getStyleClass().setAll("assistant-status");

        new Thread(() -> {
            MedicalAIService.AIResult result = MedicalAIService.predict(text);

            Platform.runLater(() -> {
                urgencyLabel.setText(result.urgency);
                emotionLabel.setText(result.emotion);

                String recommendation = result.recommendation == null || result.recommendation.isBlank()
                        ? generateAdviceFallback(result.urgency)
                        : result.recommendation;

                if (result.fallbackUsed) {
                    assistantStatusLabel.setText("Fallback analysis used because the local AI server was unavailable.");
                    assistantStatusLabel.getStyleClass().setAll("assistant-status", "assistant-status-warning");
                    if (result.errorMessage != null && !result.errorMessage.isBlank()) {
                        recommendation += "\n\nConnection detail: " + result.errorMessage;
                    }
                } else {
                    String endpoint = result.resolvedApiUrl == null ? "local AI server" : result.resolvedApiUrl;
                    assistantStatusLabel.setText("Live AI response received from " + endpoint + ".");
                    assistantStatusLabel.getStyleClass().setAll("assistant-status", "assistant-status-ok");
                }

                recommendationArea.setText(recommendation);
                applyUrgencyStyle(result.urgency);
                updateServerStatus();
            });
        }, "consultation-ai-analysis").start();
    }

    private void updateServerStatus() {
        boolean reachable = MedicalAIService.isServerReachable();
        if (reachable) {
            assistantEndpointLabel.setText("Server online: " + MedicalAIService.preferredEndpointSummary());
        } else {
            assistantEndpointLabel.setText("Server offline. Checked: " + MedicalAIService.preferredEndpointSummary());
        }
    }

    private void applyUrgencyStyle(String urgency) {
        urgencyLabel.getStyleClass().removeAll("urgency-high", "urgency-medium", "urgency-low");

        if ("HIGH".equalsIgnoreCase(urgency)) {
            urgencyLabel.getStyleClass().add("urgency-high");
        } else if ("MEDIUM".equalsIgnoreCase(urgency)) {
            urgencyLabel.getStyleClass().add("urgency-medium");
        } else {
            urgencyLabel.getStyleClass().add("urgency-low");
        }
    }

    private String generateAdviceFallback(String urgency) {
        switch (urgency) {
            case "HIGH":
                return "Potentially urgent situation. Please seek immediate medical attention or contact emergency services.";
            case "MEDIUM":
                return "A doctor consultation is recommended within the next 24 hours.";
            default:
                return "Symptoms appear less urgent. Rest, hydration, and close monitoring are recommended.";
        }
    }

    @FXML
    public void back() {
        Router.go("/fxml/doctor/doctor_list.fxml", "Mes RDV");
    }
}
