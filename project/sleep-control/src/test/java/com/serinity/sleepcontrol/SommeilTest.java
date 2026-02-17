package com.serinity.sleepcontrol;

import com.serinity.sleepcontrol.model.Sommeil;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le modèle Sommeil
 */
@DisplayName("Tests unitaires - Sommeil")
class SommeilTest {

    @Test
    @DisplayName("Sommeil - Création et propriétés")
    void sommeil_CreationEtProprietes() {
        Sommeil sommeil = new Sommeil();
        sommeil.setId(1);
        sommeil.setDateNuit(LocalDate.of(2026, 2, 17));
        sommeil.setHeureCoucher(LocalTime.of(22, 30));
        sommeil.setHeureReveil(LocalTime.of(7, 0));
        sommeil.setQualite("Bonne");
        sommeil.setInterruptions(1);
        sommeil.setHumeurReveil("Reposé");

        assertEquals(1, sommeil.getId());
        assertEquals(LocalDate.of(2026, 2, 17), sommeil.getDateNuit());
        assertEquals(LocalTime.of(22, 30), sommeil.getHeureCoucher());
        assertEquals(LocalTime.of(7, 0), sommeil.getHeureReveil());
        assertEquals("Bonne", sommeil.getQualite());
        assertEquals(1, sommeil.getInterruptions());
        assertEquals("Reposé", sommeil.getHumeurReveil());
    }

    @Test
    @DisplayName("Sommeil - Calcul durée normale (8h)")
    void sommeil_DureeNormale() {
        Sommeil sommeil = new Sommeil();
        sommeil.setHeureCoucher(LocalTime.of(22, 0));
        sommeil.setHeureReveil(LocalTime.of(6, 0));

        double duree = sommeil.getDureeSommeil();

        assertEquals(8.0, duree, 0.1);
    }

    @Test
    @DisplayName("Sommeil - Calcul durée nuit à cheval (23h30 -> 7h30)")
    void sommeil_DureeNuitACheval() {
        Sommeil sommeil = new Sommeil();
        sommeil.setHeureCoucher(LocalTime.of(23, 30));
        sommeil.setHeureReveil(LocalTime.of(7, 30));

        double duree = sommeil.getDureeSommeil();

        assertEquals(8.0, duree, 0.1);
    }

    @Test
    @DisplayName("Sommeil - Calcul durée avec minutes (22h15 -> 6h45)")
    void sommeil_DureeAvecMinutes() {
        Sommeil sommeil = new Sommeil();
        sommeil.setHeureCoucher(LocalTime.of(22, 15));
        sommeil.setHeureReveil(LocalTime.of(6, 45));

        double duree = sommeil.getDureeSommeil();

        assertEquals(8.5, duree, 0.1);
    }

    @Test
    @DisplayName("Sommeil - Score qualité excellent")
    void sommeil_ScoreExcellent() {
        Sommeil sommeil = new Sommeil();
        sommeil.setQualite("Excellente");
        sommeil.setHeureCoucher(LocalTime.of(22, 0));
        sommeil.setHeureReveil(LocalTime.of(7, 0));
        sommeil.setInterruptions(0);
        sommeil.setHumeurReveil("Énergisé");

        int score = sommeil.calculerScoreQualite();

        assertTrue(score >= 80,
                "Score excellent devrait être >= 80");
    }

    @Test
    @DisplayName("Sommeil - Score qualité moyen")
    void sommeil_ScoreMoyen() {
        Sommeil sommeil = new Sommeil();
        sommeil.setQualite("Moyenne");
        sommeil.setHeureCoucher(LocalTime.of(23, 0));
        sommeil.setHeureReveil(LocalTime.of(6, 0));
        sommeil.setInterruptions(2);
        sommeil.setHumeurReveil("Neutre");

        int score = sommeil.calculerScoreQualite();

        assertTrue(score >= 50 && score < 80,
                "Score moyen devrait être entre 50 et 80");
    }

    @Test
    @DisplayName("Sommeil - Score qualité faible")
    void sommeil_ScoreFaible() {
        Sommeil sommeil = new Sommeil();
        sommeil.setQualite("Mauvaise");
        sommeil.setHeureCoucher(LocalTime.of(2, 0));
        sommeil.setHeureReveil(LocalTime.of(5, 0));
        sommeil.setInterruptions(5);
        sommeil.setHumeurReveil("Épuisé");

        int score = sommeil.calculerScoreQualite();

        assertTrue(score < 50,
                "Score faible devrait être < 50");
    }

    @Test
    @DisplayName("Sommeil - Durée courte (moins de 5h)")
    void sommeil_DureeCourte() {
        Sommeil sommeil = new Sommeil();
        sommeil.setHeureCoucher(LocalTime.of(1, 0));
        sommeil.setHeureReveil(LocalTime.of(5, 0));

        double duree = sommeil.getDureeSommeil();

        assertTrue(duree < 5.0,
                "Durée courte devrait être < 5h");
    }

    @Test
    @DisplayName("Sommeil - Durée longue (plus de 9h)")
    void sommeil_DureeLongue() {
        Sommeil sommeil = new Sommeil();
        sommeil.setHeureCoucher(LocalTime.of(21, 0));
        sommeil.setHeureReveil(LocalTime.of(8, 0));

        double duree = sommeil.getDureeSommeil();

        assertTrue(duree > 9.0,
                "Durée longue devrait être > 9h");
    }

    @Test
    @DisplayName("Sommeil - Température minimale valide (15°C)")
    void sommeil_TemperatureMin() {
        Sommeil sommeil = new Sommeil();
        sommeil.setTemperature(15.0);

        assertEquals(15.0, sommeil.getTemperature(), 0.1);
        assertTrue(sommeil.getTemperature() >= 10.0);
    }

    @Test
    @DisplayName("Sommeil - Température maximale valide (25°C)")
    void sommeil_TemperatureMax() {
        Sommeil sommeil = new Sommeil();
        sommeil.setTemperature(25.0);

        assertEquals(25.0, sommeil.getTemperature(), 0.1);
        assertTrue(sommeil.getTemperature() <= 30.0);
    }

    @Test
    @DisplayName("Sommeil - Température idéale (20°C)")
    void sommeil_TemperatureIdeale() {
        Sommeil sommeil = new Sommeil();
        sommeil.setTemperature(20.0);

        assertEquals(20.0, sommeil.getTemperature(), 0.1);
    }

    @Test
    @DisplayName("Sommeil - Aucune interruption")
    void sommeil_AucuneInterruption() {
        Sommeil sommeil = new Sommeil();
        sommeil.setInterruptions(0);

        assertEquals(0, sommeil.getInterruptions());
    }

    @Test
    @DisplayName("Sommeil - Interruptions multiples (5)")
    void sommeil_InterruptionsMultiples() {
        Sommeil sommeil = new Sommeil();
        sommeil.setInterruptions(5);

        assertEquals(5, sommeil.getInterruptions());
        assertTrue(sommeil.getInterruptions() > 3);
    }

    @Test
    @DisplayName("Sommeil - Interruptions impactent le score")
    void sommeil_InterruptionsImpactScore() {
        Sommeil sommeil1 = new Sommeil();
        sommeil1.setQualite("Bonne");
        sommeil1.setHeureCoucher(LocalTime.of(22, 0));
        sommeil1.setHeureReveil(LocalTime.of(6, 0));
        sommeil1.setInterruptions(0);
        sommeil1.setHumeurReveil("Reposé");

        Sommeil sommeil2 = new Sommeil();
        sommeil2.setQualite("Bonne");
        sommeil2.setHeureCoucher(LocalTime.of(22, 0));
        sommeil2.setHeureReveil(LocalTime.of(6, 0));
        sommeil2.setInterruptions(5);
        sommeil2.setHumeurReveil("Reposé");

        int score1 = sommeil1.calculerScoreQualite();
        int score2 = sommeil2.calculerScoreQualite();

        assertTrue(score1 > score2,
                "Score sans interruptions doit être meilleur");
    }




    @Test
    @DisplayName("Sommeil - Qualité excellente")
    void sommeil_QualiteExcellente() {
        Sommeil sommeil = new Sommeil();
        sommeil.setQualite("Excellente");

        assertEquals("Excellente", sommeil.getQualite());
    }

    @Test
    @DisplayName("Sommeil - Qualité mauvaise")
    void sommeil_QualiteMauvaise() {
        Sommeil sommeil = new Sommeil();
        sommeil.setQualite("Mauvaise");

        assertEquals("Mauvaise", sommeil.getQualite());
    }

    @Test
    @DisplayName("Sommeil - Humeur énergisé")
    void sommeil_HumeurEnergie() {
        Sommeil sommeil = new Sommeil();
        sommeil.setHumeurReveil("Énergisé");

        assertEquals("Énergisé", sommeil.getHumeurReveil());
    }

    @Test
    @DisplayName("Sommeil - Humeur épuisé")
    void sommeil_HumeurEpuise() {
        Sommeil sommeil = new Sommeil();
        sommeil.setHumeurReveil("Épuisé");

        assertEquals("Épuisé", sommeil.getHumeurReveil());
    }

    @Test
    @DisplayName("Sommeil - Environnement calme")
    void sommeil_EnvironnementCalme() {
        Sommeil sommeil = new Sommeil();
        sommeil.setEnvironnement("Calme");

        assertEquals("Calme", sommeil.getEnvironnement());
    }

    @Test
    @DisplayName("Sommeil - Niveau de bruit silencieux")
    void sommeil_BruitSilencieux() {
        Sommeil sommeil = new Sommeil();
        sommeil.setNiveauBruit("Silencieux");

        assertEquals("Silencieux", sommeil.getNiveauBruit());
    }

    @Test
    @DisplayName("Sommeil - Niveau de bruit fort")
    void sommeil_BruitFort() {
        Sommeil sommeil = new Sommeil();
        sommeil.setNiveauBruit("Fort");

        assertEquals("Fort", sommeil.getNiveauBruit());
    }

    @Test
    @DisplayName("Sommeil - Commentaire ajouté")
    void sommeil_CommentaireAjoute() {
        Sommeil sommeil = new Sommeil();
        sommeil.setCommentaire("Bonne nuit de sommeil");

        assertEquals("Bonne nuit de sommeil", sommeil.getCommentaire());
        assertNotNull(sommeil.getCommentaire());
    }

    @Test
    @DisplayName("Validation - Durée cohérente (0-24h)")
    void validation_DureeCoherente() {
        Sommeil sommeil = new Sommeil();
        sommeil.setHeureCoucher(LocalTime.of(22, 0));
        sommeil.setHeureReveil(LocalTime.of(6, 0));

        double duree = sommeil.getDureeSommeil();

        assertTrue(duree > 0 && duree <= 24,
                "Durée doit être entre 0 et 24h");
    }

    @Test
    @DisplayName("Sommeil complet - Toutes les propriétés")
    void sommeil_Complet() {
        Sommeil sommeil = new Sommeil();
        sommeil.setId(1);
        sommeil.setDateNuit(LocalDate.now().minusDays(1));
        sommeil.setHeureCoucher(LocalTime.of(22, 30));
        sommeil.setHeureReveil(LocalTime.of(7, 0));
        sommeil.setQualite("Excellente");
        sommeil.setInterruptions(0);
        sommeil.setHumeurReveil("Énergisé");
        sommeil.setTemperature(20.0);
        sommeil.setNiveauBruit("Silencieux");
        sommeil.setEnvironnement("Calme");
        sommeil.setCommentaire("Excellente nuit");

        assertNotNull(sommeil);
        assertEquals(1, sommeil.getId());
        assertNotNull(sommeil.getDateNuit());
        assertNotNull(sommeil.getHeureCoucher());
        assertNotNull(sommeil.getHeureReveil());
        assertNotNull(sommeil.getQualite());
        assertEquals(0, sommeil.getInterruptions());
        assertNotNull(sommeil.getHumeurReveil());
        assertTrue(sommeil.getTemperature() > 0);
        assertNotNull(sommeil.getNiveauBruit());
        assertNotNull(sommeil.getEnvironnement());
        assertTrue(sommeil.getNombreReves() >= 0);
        assertNotNull(sommeil.getCommentaire());
    }
}
