package com.serinity.accesscontrol.service.exercice;

import com.serinity.accesscontrol.service.exercice.api.WeatherApiService;

import java.time.LocalTime;

public class FatigueEstimationService {

    /**
     * Combine un score utilisateur (slider) + contexte (heure + météo).
     * Retourne fatigue 0..100 (plus haut = plus fatigué).
     */
    public int estimate(int userFatigue, WeatherApiService.WeatherNow w) {
        int f = clamp(userFatigue);

        // Heure : nuit / soir -> plus de fatigue
        int hour = LocalTime.now().getHour();
        if (hour >= 22 || hour < 6) f += 15;
        else if (hour >= 18) f += 8;

        // Pluie / vent -> tendance à fatigue mentale / motivation plus faible
        if (w.isRaining()) f += 10;
        if (w.windKph() >= 30) f += 5;

        // Petit ajustement: très froid ou très chaud
        if (w.temperatureC() <= 8) f += 5;
        if (w.temperatureC() >= 32) f += 5;

        return clamp(f);
    }

    private int clamp(int v) {
        if (v < 0) return 0;
        if (v > 100) return 100;
        return v;
    }
}