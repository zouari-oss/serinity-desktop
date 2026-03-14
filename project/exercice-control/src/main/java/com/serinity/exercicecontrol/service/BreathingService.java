package com.serinity.exercicecontrol.service;

import com.serinity.exercicecontrol.model.BreathingPlan;
import com.serinity.exercicecontrol.model.BreathingProtocol;
import com.serinity.exercicecontrol.model.BreathingState;

public class BreathingService {

    public BreathingPlan recommend(BreathingState state) {
        return switch (state) {
            case STRESS_AIGU -> new BreathingPlan(
                    state,
                    boxBreathing(180), // 3 min
                    "Stress aigu → protocole court et structuré pour te recentrer rapidement."
            );
            case FATIGUE -> new BreathingPlan(
                    state,
                    coherence(300), // 5 min
                    "Fatigue → respiration douce et régulière, sans effort."
            );
            case RUMINATIONS -> new BreathingPlan(
                    state,
                    slowExhale(420), // 7 min
                    "Ruminations → expiration plus longue pour calmer le mental."
            );
            case NORMAL -> new BreathingPlan(
                    state,
                    coherence(300),
                    "Routine → cohérence cardiaque 5 minutes."
            );
        };
    }

    private BreathingProtocol coherence(int totalSeconds) {
        // 5/0/5/0 = 6 respirations/min
        return new BreathingProtocol(
                "Cohérence cardiaque (6/min)",
                totalSeconds,
                5, 0, 5, 0,
                "Calme, stabilité, meilleure régulation du stress"
        );
    }

    private BreathingProtocol boxBreathing(int totalSeconds) {
        // 4/4/4/4
        return new BreathingProtocol(
                "Box breathing (4-4-4-4)",
                totalSeconds,
                4, 4, 4, 4,
                "Réduction rapide du stress, focus"
        );
    }

    private BreathingProtocol slowExhale(int totalSeconds) {
        // 4/0/6/0
        return new BreathingProtocol(
                "Respiration lente (4/6)",
                totalSeconds,
                4, 0, 6, 0,
                "Apaisement, baisse des ruminations"
        );
    }
} 