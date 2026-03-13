package com.serinity.accesscontrol.service.exercice;

public class ContextAwarePlanner {

    public record DailyContext(
            boolean raining,
            boolean eveningOrNight,
            int fatigueScore,        // 0..100
            double temperatureC,
            double windKph
    ) {}

    public record DailyPlan(
            String contextLine,
            String activityTitle,
            int activityMinutes,
            String meditationTitle,
            int meditationMinutes,
            String youtubeQuery,
            String why
    ) {}
    public DailyPlan build(DailyContext c) {
        boolean tired = c.fatigueScore() >= 60;

        boolean extremeTemp = (c.temperatureC() <= 8) || (c.temperatureC() >= 32);
        boolean strongWind  = c.windKph() >= 30;

        boolean indoorForced = c.raining() || extremeTemp || strongWind;

        // 1) Fatigue + soir/nuit => priorité sommeil
        if (tired && c.eveningOrNight()) {
            return new DailyPlan(
                    contextLine(c, indoorForced),
                    indoorForced ? "Yoga très doux indoor" : "Marche très douce",
                    indoorForced ? 8 : 10,
                    "Sleep meditation (priorité sommeil)",
                    10,
                    "sleep meditation 10 minutes",
                    "Fatigue + soir/nuit → on favorise le sommeil (calme du système nerveux + récupération)."
            );
        }

        // 2) Indoor obligatoire (pluie/temp extrême/vent fort) + fatigue
        if (indoorForced && tired) {
            return new DailyPlan(
                    contextLine(c, true),
                    "Yoga doux indoor",
                    10,
                    "Respiration guidée anti-stress",
                    7,
                    "gentle yoga for stress 10 minutes + guided breathing 7 minutes",
                    "Conditions extérieures défavorables → indoor. Fatigue → intensité basse + respiration calmante."
            );
        }

        // 3) Indoor obligatoire (même sans fatigue)
        if (indoorForced) {
            return new DailyPlan(
                    contextLine(c, true),
                    "Stretching / mobilité indoor",
                    12,
                    c.eveningOrNight() ? "Méditation du soir" : "Mindfulness courte",
                    c.eveningOrNight() ? 8 : 5,
                    c.eveningOrNight() ? "evening mindfulness meditation 8 minutes" : "mindfulness meditation 5 minutes",
                    "Conditions extérieures défavorables → on privilégie une séance indoor simple et efficace."
            );
        }

        // 4) Fatigue sans contrainte météo : marche douce + respiration
        if (tired) {
            return new DailyPlan(
                    contextLine(c, false),
                    "Marche douce",
                    15,
                    "Respiration guidée",
                    5,
                    "breathing meditation 5 minutes",
                    "Fatigue → intensité basse + respiration courte pour calmer."
            );
        }

        // 5) Bon état
        return new DailyPlan(
                contextLine(c, false),
                "Marche active",
                20,
                "Méditation focus",
                5,
                "guided meditation focus 5 minutes",
                "Bon état → activité cardio légère + méditation courte."
        );
    }

    private String contextLine(DailyContext c, boolean indoorForced) {
        StringBuilder sb = new StringBuilder();

        // météo
        if (c.raining()) sb.append("Pluie");
        else sb.append("Temps OK");

        // contraintes
        if (c.windKph() >= 30) sb.append(" + vent fort");
        if (c.temperatureC() <= 8) sb.append(" + froid");
        if (c.temperatureC() >= 32) sb.append(" + chaleur");

        // indoor
        if (indoorForced) sb.append(" → indoor");

        // fatigue
        sb.append(" • fatigue ").append(c.fatigueScore()).append("/100");

        return sb.toString();
    }
}