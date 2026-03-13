package com.serinity.accesscontrol.controller.sleepcontrol;

import com.serinity.accesscontrol.model.sleepcontrol.WeatherData;
import com.serinity.accesscontrol.service.sleepcontrol.WeatherService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class WeatherController {

    @FXML private Label lblVille;
    @FXML private Label lblTemp;
    @FXML private Label lblRessentie;
    @FXML private Label lblHumidite;
    @FXML private Label lblVent;
    @FXML private Label lblDescription;
    @FXML private Label lblEmoji;
    @FXML private Label lblImpact;
    @FXML private Label lblConseil;
    @FXML private Label lblAlerte;
    @FXML private Label lblStatut;
    @FXML private VBox  meteoContainer;

    private final WeatherService weatherService = new WeatherService();

    @FXML
    public void initialize() {
        chargerMeteo();
    }

    @FXML
    private void actualiser() {
        chargerMeteo();
    }

    private void chargerMeteo() {
        lblStatut.setText("⏳ Chargement...");
        meteoContainer.setDisable(true);

        // Appel API dans un thread séparé
        new Thread(() -> {
            WeatherData data = weatherService.getMeteoActuelle();
            Platform.runLater(() -> afficherMeteo(data));
        }).start();
    }

    private void afficherMeteo(WeatherData data) {
        meteoContainer.setDisable(false);

        if (!data.isChargee()) {
            lblStatut.setText("❌ " + data.getErreur());
            lblStatut.setStyle("-fx-text-fill: #dc2626;");
            return;
        }

        lblStatut.setText("✅ Mis à jour");
        lblStatut.setStyle("-fx-text-fill: #16a34a;");

        String emoji = WeatherService.getEmojiMeteo(data.getIcone());
        lblEmoji.setText(emoji);
        lblVille.setText("📍 " + data.getVille());
        lblTemp.setText(String.format("%.1f°C", data.getTemperature()));
        lblRessentie.setText(String.format("Ressenti : %.1f°C",
                data.getTemperatureRessentie()));
        lblHumidite.setText(String.format("💧 Humidité : %.0f%%",
                data.getHumidite()));
        lblVent.setText(String.format("💨 Vent : %.0f km/h",
                data.getVitesseVent()));
        lblDescription.setText(data.getDescription());

        // Impact sommeil
        lblImpact.setText(data.evaluerImpactSommeil());
        lblConseil.setText(data.getConseilSommeil());

        // Niveau alerte
        lblAlerte.setText("Alerte : " + data.getNiveauAlerte());
        lblAlerte.getStyleClass().removeAll(
                "alerte-aucun", "alerte-faible", "alerte-modere", "alerte-eleve");
        lblAlerte.getStyleClass().add(switch (data.getNiveauAlerte()) {
            case "ÉLEVÉ"  -> "alerte-eleve";
            case "MODÉRÉ" -> "alerte-modere";
            default       -> "alerte-aucun";
        });
    }
}
