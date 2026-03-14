package com.serinity.exercicecontrol.service.ai;

import java.util.List;

/**
 * Réponse “coach” générée par l'IA, prête à afficher dans l'UI.
 * Format adapté à une sortie JSON structurée.
 */
public record CoachAnswer(
        String summary,
        List<String> actions,
        NextSession nextSession,
        String safetyNote
) {

    public record NextSession(
            String warmup,
            String main,
            String cooldown
    ) {}
}