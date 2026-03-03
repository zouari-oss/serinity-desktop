package com.serinity.exercicecontrol.service.performance;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.serinity.exercicecontrol.dao.ExerciseSessionDao.SessionSummary;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.serinity.exercicecontrol.service.performance.PerformanceReport.*;

public class PerformanceAnalyzer {

    public PerformanceReport analyze(int userId, Integer exerciseId, int windowDays, List<SessionSummary> sessions) {
        if (sessions == null) sessions = List.of();

        // Normalise + trie du plus ancien au plus récent
        List<Row> rows = sessions.stream()
                .filter(s -> s.userId() == userId)
                .map(this::toRow)
                .sorted(Comparator.comparing(Row::day)) // ✅ record accessor
                .toList();

        int total = rows.size();
        int completed = (int) rows.stream().filter(Row::completed).count(); // ✅ accessor
        int completionRate = total == 0 ? 0 : (completed * 100) / total;

        int avgActive = (int) Math.round(rows.stream().mapToInt(Row::activeSeconds).average().orElse(0));

        // Série journalière
        List<DailyPoint> daily = buildDaily(rows);

        // Trend : compare 7 derniers jours vs 7 jours précédents
        double deltaPct = computeTrendDeltaPercent(daily);

        Status status = classify(deltaPct);

        boolean painFlag = rows.stream().anyMatch(r -> r.pain() != null && r.pain() >= 6);
        boolean rpeUp = isRpeUp(rows);
        boolean fatigueFlag = (status == Status.REGRESSION && (rpeUp || completionRate < 60));

        List<Recommendation> recs = new ArrayList<>();

        if (painFlag) {
            recs.add(new Recommendation(Recommendation.Type.SAFETY,
                    "Douleur élevée détectée. Réduis fortement l’intensité/arrête si ça fait mal et parle à un adulte/pro."));
        }

        if (fatigueFlag) {
            recs.add(new Recommendation(Recommendation.Type.DELOAD,
                    "Deload recommandé 7 jours : -30% à -50% de volume (moins de séries ou moins de temps), garde une intensité confortable."));
            recs.add(new Recommendation(Recommendation.Type.REST,
                    "Ajoute 1 jour de repos ou une séance très légère (respiration/étirements doux)."));
        } else if (status == Status.PROGRESSION) {
            recs.add(new Recommendation(Recommendation.Type.PROGRESSION,
                    "Progression OK : augmente doucement UNE seule chose (ex: +5% reps OU +1 série OU +60s)."));
        } else {
            recs.add(new Recommendation(Recommendation.Type.CONSISTENCY,
                    "Stagnation : garde la régularité + ajuste petit à petit (échauffement, meilleure exécution, sommeil)."));
        }

        NextSessionPlan plan = buildNextPlan(status, fatigueFlag, painFlag, avgActive, rows);

        return new PerformanceReport(
                userId,
                exerciseId,
                windowDays,
                total,
                completed,
                completionRate,
                avgActive,
                round1(deltaPct),
                status,
                fatigueFlag,
                painFlag,
                recs,
                plan,
                daily
        );
    }

    // ---------------- internals ----------------

    private record Row(LocalDate day, boolean completed, int activeSeconds, Integer reps, Integer rpe, Integer pain) {}

    private Row toRow(SessionSummary s) {
        LocalDate day = toDay(s);
        boolean completed = s.status() != null && s.status().equalsIgnoreCase("COMPLETED");
        int active = Math.max(0, s.activeSeconds());

        FeedbackMetrics m = parseFeedback(s.feedback());
        return new Row(day, completed, active, m.reps, m.rpe, m.pain);
    }

    private static LocalDate toDay(SessionSummary s) {
        var dt = s.startedAt() != null ? s.startedAt() : (s.completedAt() != null ? s.completedAt() : null);
        if (dt == null) return LocalDate.now(ZoneId.systemDefault());
        return dt.toLocalDate();
    }

    private List<DailyPoint> buildDaily(List<Row> rows) {
        Map<LocalDate, List<Row>> byDay = rows.stream()
                .collect(Collectors.groupingBy(Row::day, TreeMap::new, Collectors.toList())); // ✅ accessor

        List<DailyPoint> out = new ArrayList<>();
        for (var e : byDay.entrySet()) {
            int sessions = e.getValue().size();
            int completed = (int) e.getValue().stream().filter(Row::completed).count();
            int active = e.getValue().stream().mapToInt(Row::activeSeconds).sum();

            // ✅ LocalDate -> String (évite crash Gson/JPMS)
            out.add(new DailyPoint(e.getKey().toString(), sessions, completed, active));
        }
        return out;
    }

    private double computeTrendDeltaPercent(List<DailyPoint> daily) {
        if (daily.isEmpty()) return 0.0;

        List<DailyPoint> tail = daily.size() <= 14 ? daily : daily.subList(daily.size() - 14, daily.size());
        List<DailyPoint> first = tail.size() <= 7 ? List.of() : tail.subList(0, tail.size() - 7);
        List<DailyPoint> last = tail.size() <= 7 ? tail : tail.subList(tail.size() - 7, tail.size());

        double avg1 = first.isEmpty() ? 0.0 : first.stream().mapToInt(DailyPoint::activeSeconds).average().orElse(0);
        double avg2 = last.stream().mapToInt(DailyPoint::activeSeconds).average().orElse(0);

        if (avg1 <= 0.0) return 0.0;
        return ((avg2 - avg1) / avg1) * 100.0;
    }

    private Status classify(double deltaPct) {
        if (deltaPct >= 3.0) return Status.PROGRESSION;
        if (deltaPct <= -3.0) return Status.REGRESSION;
        return Status.STAGNATION;
    }

    private boolean isRpeUp(List<Row> rows) {
        List<Integer> rpes = rows.stream().map(Row::rpe).filter(Objects::nonNull).toList();
        if (rpes.size() < 6) return false;

        List<Integer> tail = rpes.size() <= 14 ? rpes : rpes.subList(rpes.size() - 14, rpes.size());
        List<Integer> first = tail.size() <= 7 ? List.of() : tail.subList(0, tail.size() - 7);
        List<Integer> last = tail.size() <= 7 ? tail : tail.subList(tail.size() - 7, tail.size());

        if (first.isEmpty()) return false;

        double a = first.stream().mapToInt(i -> i).average().orElse(0);
        double b = last.stream().mapToInt(i -> i).average().orElse(0);
        return (b - a) >= 1.0;
    }

    private NextSessionPlan buildNextPlan(Status status, boolean fatigue, boolean pain, int avgActive, List<Row> rows) {
        int base = Math.max(60, avgActive);

        Integer lastReps = rows.stream().map(Row::reps).filter(Objects::nonNull).reduce((a, b) -> b).orElse(null);
        Integer lastRpe  = rows.stream().map(Row::rpe).filter(Objects::nonNull).reduce((a, b) -> b).orElse(null);

        if (pain) {
            return new NextSessionPlan(
                    "Séance très légère (sécurité d’abord)",
                    Math.max(60, (int) Math.round(base * 0.5)),
                    lastReps == null ? null : (int) Math.round(lastReps * 0.7),
                    4
            );
        }

        if (fatigue) {
            return new NextSessionPlan(
                    "Deload (baisser charge/volume)",
                    Math.max(60, (int) Math.round(base * 0.6)),
                    lastReps == null ? null : (int) Math.round(lastReps * 0.7),
                    lastRpe == null ? 6 : Math.min(6, lastRpe)
            );
        }

        if (status == Status.PROGRESSION) {
            return new NextSessionPlan(
                    "Progression douce (1 seul paramètre)",
                    (int) Math.round(base * 1.05),
                    lastReps == null ? null : (int) Math.round(lastReps * 1.05),
                    lastRpe == null ? 7 : Math.min(8, lastRpe + 1)
            );
        }

        return new NextSessionPlan(
                "Stabilité + technique",
                base,
                lastReps,
                lastRpe == null ? 7 : lastRpe
        );
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    // ------- feedback parsing (optional JSON) -------

    private static final class FeedbackMetrics {
        Integer reps;
        Integer rpe;
        Integer pain;
    }

    private FeedbackMetrics parseFeedback(String feedback) {
        FeedbackMetrics m = new FeedbackMetrics();
        if (feedback == null || feedback.isBlank()) return m;

        try {
            var el = JsonParser.parseString(feedback.trim());
            if (el.isJsonObject()) {
                JsonObject o = el.getAsJsonObject();
                m.reps = getInt(o, "reps");
                m.rpe  = getInt(o, "rpe");
                m.pain = getInt(o, "pain");
            }
        } catch (Exception ignored) {}
        return m;
    }

    private static Integer getInt(JsonObject o, String key) {
        try {
            if (!o.has(key) || o.get(key).isJsonNull()) return null;
            return o.get(key).getAsInt();
        } catch (Exception e) {
            return null;
        }
    }
}