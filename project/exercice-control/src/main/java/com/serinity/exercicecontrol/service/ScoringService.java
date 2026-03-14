package com.serinity.exercicecontrol.service;

import com.serinity.exercicecontrol.dao.ScoringDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ScoringService {

    private final ScoringDAO dao;

    public ScoringService(ScoringDAO dao) {
        this.dao = dao;
    }

    public ScoreResult computeEngagementScore(int userId) {
        try {
            int started = dao.countStarted(userId);
            int completed = dao.countCompleted(userId);
            int active7d = dao.sumActiveSecondsLast7Days(userId);
            int streak = computeStreak(dao.getCompletedDaysDesc(userId));

            int completionRate = (started == 0) ? 0 : (completed * 100) / started;

            // نقاط /100 (simple à expliquer)
            int streakPts = clamp((streak * 3), 0, 30);                 // 0..10 jours -> 0..30
            int completionPts = clamp((completionRate * 40) / 100, 0, 40); // 0..100% -> 0..40
            int timePts = clamp((active7d * 30) / (30 * 60), 0, 30);       // 0..30 min -> 0..30

            int total = streakPts + completionPts + timePts;

            return new ScoreResult(
                    total,
                    streak,
                    completionRate,
                    active7d
            );

        } catch (SQLException e) {
            throw new RuntimeException("Erreur calcul score: " + e.getMessage(), e);
        }
    }

    private int computeStreak(List<LocalDate> daysDesc) {
        if (daysDesc == null || daysDesc.isEmpty()) return 0;

        LocalDate expected = LocalDate.now();
        int streak = 0;

        for (LocalDate d : daysDesc) {
            if (d.equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else if (d.isBefore(expected)) {
                break; // première coupure
            }
        }
        return streak;
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    public record ScoreResult(
            int score100,
            int streakDays,
            int completionRatePercent,
            int activeSeconds7d
    ) {
        public String activeTime7dText() {
            int m = activeSeconds7d / 60;
            int s = activeSeconds7d % 60;
            return m + " min " + s + " s";
        }

        public String levelText() {
            if (score100 >= 70) return "Excellent";
            if (score100 >= 40) return "Bon";
            return "À renforcer";
        }
    }
}