package com.serinity.exercicecontrol.service;

import com.serinity.exercicecontrol.model.ActionPlan;
import com.serinity.exercicecontrol.model.Exercise;
import com.serinity.exercicecontrol.model.ExerciseSession;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import com.serinity.exercicecontrol.dao.ExerciseSessionDao;
public class BehaviorChangeService {

    private final ExerciseSessionDao sessionDao;

    public BehaviorChangeService() {
        this.sessionDao = new ExerciseSessionDao();
    }

    public ActionPlan buildActionPlan(int userId, Exercise exercise, int targetMinutes) {
        int exerciseMinutes = exercise != null ? exercise.getDurationMinutes() : targetMinutes;

        int plannedMinutes = gradedMinutes(userId, targetMinutes, exerciseMinutes);
        LocalTime plannedTime = suggestTimeSlot(userId);

        String microCommitment =
                plannedMinutes <= 5
                        ? "Juste 5 minutes. Le plus dur est de commencer."
                        : (plannedMinutes <= 8
                        ? "Une petite séance pour garder le rythme."
                        : "Vous êtes prêt pour un petit défi aujourd’hui.");

        // ✅ streak intelligent (basé sur completedAt)
        StreakInfo streak = computeStreakInfo(userId);

        return new ActionPlan(
                plannedMinutes,
                plannedTime,
                microCommitment,
                streak.days,
                streak.message
        );
    }

    // ===================== STREAK =====================

    private static class StreakInfo {
        final int days;
        final String message;
        StreakInfo(int days, String message) {
            this.days = days;
            this.message = message;
        }
    }

    /**
     * Streak = jours consécutifs avec au moins 1 session COMPLETED.
     * On utilise completedAt (plus fiable).
     *
     * Messages :
     * - Si aujourd’hui complété : streak actif
     * - Si hier complété mais pas aujourd’hui : à prolonger
     * - Sinon : encourager à démarrer une série
     */
    private StreakInfo computeStreakInfo(int userId) {
        try {
            List<ExerciseSession> sessions = sessionDao.findByUserId(userId);

            Set<LocalDate> completedDays = new HashSet<>();

            for (ExerciseSession s : sessions) {
                String st = s.getStatus() == null ? "" : s.getStatus();

                if (!"COMPLETED".equalsIgnoreCase(st)) continue;

                LocalDateTime completedAt = s.getCompletedAt();
                if (completedAt == null) continue;

                completedDays.add(completedAt.toLocalDate());
            }

            if (completedDays.isEmpty()) {
                return new StreakInfo(0, "🌱 Démarre une série : 1 séance aujourd’hui.");
            }

            LocalDate today = LocalDate.now();
            boolean doneToday = completedDays.contains(today);
            boolean doneYesterday = completedDays.contains(today.minusDays(1));

            LocalDate end = doneToday ? today : (doneYesterday ? today.minusDays(1) : null);
            if (end == null) {
                return new StreakInfo(0, "🌱 Démarre une série : 1 séance aujourd’hui.");
            }

            int streak = 0;
            LocalDate d = end;
            while (completedDays.contains(d)) {
                streak++;
                d = d.minusDays(1);
            }

            // Message intelligent
            if (doneToday) {
                if (streak >= 7) return new StreakInfo(streak, "🏆 Série : " + streak + " jours ! Excellent !");
                if (streak >= 3) return new StreakInfo(streak, "🔥 Série : " + streak + " jours ! Continue !");
                return new StreakInfo(streak, "✅ Série : " + streak + " jour(s) — bien joué !");
            } else {
                // hier validé mais pas aujourd’hui
                if (streak >= 7) return new StreakInfo(streak, "⏳ Série : " + streak + " jours — garde le rythme aujourd’hui !");
                if (streak >= 3) return new StreakInfo(streak, "⏳ Série : " + streak + " jours — ne casse pas la chaîne aujourd’hui !");
                return new StreakInfo(streak, "⏳ Série : " + streak + " jour(s) — valide aujourd’hui pour continuer.");
            }

        } catch (SQLException e) {
            return new StreakInfo(0, "🌱 Démarre une série : 1 séance aujourd’hui.");
        }
    }

    // ===================== GRADED TASKS =====================

    private int gradedMinutes(int userId, int targetMinutes, int exerciseMinutes) {
        int base = Math.max(3, Math.min(targetMinutes, exerciseMinutes > 0 ? exerciseMinutes : targetMinutes));

        try {
            List<ExerciseSession> sessions = sessionDao.findByUserId(userId);
            LocalDateTime cutoff = LocalDateTime.now().minusDays(14);

            int started = 0;
            int completed = 0;

            for (ExerciseSession s : sessions) {
                LocalDateTime startedAt = s.getStartedAt();
                if (startedAt == null) continue;

                // si ta liste n’est PAS triée DESC, enlève ce break
                if (startedAt.isBefore(cutoff)) break;

                String st = s.getStatus() == null ? "" : s.getStatus();

                if ("STARTED".equalsIgnoreCase(st)
                        || "COMPLETED".equalsIgnoreCase(st)
                        || "ABANDONED".equalsIgnoreCase(st)) {
                    started++;
                }
                if ("COMPLETED".equalsIgnoreCase(st)) completed++;
            }

            if (started >= 3) {
                int completionRate = (completed * 100) / started;
                if (completionRate < 40) return Math.max(3, base - 3);
                if (completionRate < 60) return Math.max(3, base - 1);
                if (completionRate >= 80) {
                    return (exerciseMinutes > 0) ? Math.min(exerciseMinutes, base + 1) : base + 1;
                }
            }
        } catch (SQLException ignored) {}

        return base;
    }

    // ===================== ACTION PLANNING =====================

    private LocalTime suggestTimeSlot(int userId) {
        try {
            List<ExerciseSession> sessions = sessionDao.findByUserId(userId);
            LocalDateTime cutoff = LocalDateTime.now().minusDays(30);

            Map<Integer, Integer> hourCount = new HashMap<>();

            for (ExerciseSession s : sessions) {
                LocalDateTime startedAt = s.getStartedAt();
                if (startedAt == null) continue;

                // si ta liste n’est PAS triée DESC, enlève ce break
                if (startedAt.isBefore(cutoff)) break;

                int h = startedAt.getHour();
                hourCount.put(h, hourCount.getOrDefault(h, 0) + 1);
            }

            if (!hourCount.isEmpty()) {
                int bestHour = 21;
                int best = -1;
                for (Map.Entry<Integer, Integer> e : hourCount.entrySet()) {
                    if (e.getValue() > best) {
                        best = e.getValue();
                        bestHour = e.getKey();
                    }
                }
                return LocalTime.of(bestHour, 0);
            }
        } catch (SQLException ignored) {}

        return LocalTime.of(21, 0);
    }
}