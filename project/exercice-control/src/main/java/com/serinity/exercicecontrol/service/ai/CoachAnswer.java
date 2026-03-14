package com.serinity.exercicecontrol.service.ai;

import java.util.List;

/**
 * Réponse “coach” générée par l'IA, prête à afficher dans l'UI.
 * Format adapté à une sortie JSON structurée.
 *
 * @param summary résumé motivant de la séance
 * @param actions actions concrètes recommandées
 * @param nextSession proposition structurée pour la prochaine séance
 * @param safetyNote consigne de sécurité à rappeler
 */
public record CoachAnswer(
        String summary,
        List<String> actions,
        NextSession nextSession,
        String safetyNote
) {

  /**
   * Détails de la prochaine séance.
   *
   * @param warmup proposition d'échauffement
   * @param main activité principale
   * @param cooldown retour au calme conseillé
   */
    public record NextSession(
            String warmup,
            String main,
            String cooldown
    ) {}
}
