package com.serinity.exercicecontrol.service;

import com.serinity.exercicecontrol.dao.RecommendationDAO;
import com.serinity.exercicecontrol.model.Exercise;

import java.sql.SQLException;
import java.util.List;

public class RecommendationService {

    private final RecommendationDAO dao;

    public RecommendationService(RecommendationDAO dao) {
        this.dao = dao;
    }

    /**
     * Recommandation simple:
     * - évite l'exercice déjà fait aujourd’hui
     * - favorise "jamais essayé"
     * - pénalise "souvent abandonné"
     * - adapte la durée selon le score d’engagement
     */
    public RecommendationResult recommend(int userId, int engagementScore100, List<Exercise> exercises) {
        if (exercises == null || exercises.isEmpty()) return null;

        int targetMinutes = targetMinutesForScore(engagementScore100);

        Exercise best = null;
        int bestScore = Integer.MIN_VALUE;
        String bestReason = "—";

        for (Exercise ex : exercises) {
            try {
                int exerciseId = ex.getId();

                // éviter répétition
                if (dao.doneToday(userId, exerciseId)) continue;

                int started = dao.countStarted(userId, exerciseId);
                int completed = dao.countCompleted(userId, exerciseId);
                int aborted = dao.countAborted(userId, exerciseId);

                int completionRate = started == 0 ? 0 : (completed * 100) / started;
                int abandonRate = started == 0 ? 0 : (aborted * 100) / started;

                int score = 0;
                String reason = "";

                // jamais essayé
                if (started == 0) {
                    score += 25;
                    reason += "Nouveau pour vous • ";
                }

                // réussite
                if (completionRate >= 70) {
                    score += 15;
                    reason += "Souvent terminé • ";
                } else if (completionRate > 0) {
                    score += 5;
                }

                // abandon
                if (abandonRate >= 50) {
                    score -= 25;
                    reason += "Souvent abandonné • ";
                }

                // durée adaptée au score global
                int dur = ex.getDurationMinutes(); // dans ton model
                int diff = Math.abs(dur - targetMinutes);
                if (diff == 0) {
                    score += 20;
                    reason += "Durée idéale • ";
                } else if (diff <= 2) {
                    score += 10;
                    reason += "Durée adaptée • ";
                } else if (diff >= 6) {
                    score -= 10;
                }

                // petit bonus si cohérent avec niveau (optionnel)
                if (engagementScore100 < 40 && ex.getLevel() <= 2) score += 5;
                if (engagementScore100 >= 70 && ex.getLevel() >= 3) score += 5;

                if (score > bestScore) {
                    bestScore = score;
                    best = ex;
                    bestReason = reason.isBlank() ? "Recommandation basée sur votre activité récente." : reason.substring(0, reason.length() - 3);
                }

            } catch (SQLException e) {
                // ignorer un exercice si erreur ponctuelle
            }
        }

        if (best == null) {
            // fallback : premier exercice
            Exercise ex = exercises.get(0);
            return new RecommendationResult(ex, "Suggestion par défaut (aucune donnée suffisante).", targetMinutes);
        }

        return new RecommendationResult(best, bestReason, targetMinutes);
    }

    private int targetMinutesForScore(int score100) {
        if (score100 < 40) return 5;    // faible engagement → court
        if (score100 < 70) return 8;    // moyen → modéré
        return 12;                      // bon engagement → un peu plus long
    }

    public record RecommendationResult(Exercise exercise, String reason, int targetMinutes) {}
}