package com.serinity.exercicecontrol.service.performance;

import java.util.List;

public record PerformanceReport(
        int userId,
        Integer exerciseId,
        int windowDays,
        int sessionsTotal,
        int sessionsCompleted,
        int completionRatePercent,
        int avgActiveSeconds,
        double trendDeltaPercent,
        Status status,
        boolean fatigueFlag,
        boolean painFlag,
        List<Recommendation> recommendations,
        NextSessionPlan nextSessionPlan,
        List<DailyPoint> dailySeries
) {
    public enum Status { PROGRESSION, STAGNATION, REGRESSION }

    public record Recommendation(Type type, String details) {
        public enum Type { PROGRESSION, DELOAD, REST, CONSISTENCY, SAFETY }
    }

    public record NextSessionPlan(
            String objective,
            int targetActiveSeconds,
            Integer targetReps,
            Integer targetRpe
    ) {}

    // ✅ IMPORTANT: day en String (pas LocalDate) + activeSeconds match exactement l'analyzer
    public record DailyPoint(
            String day,
            int sessions,
            int completed,
            int activeSeconds
    ) {}
}