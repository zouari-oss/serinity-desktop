package com.serinity.sleepcontrol;

import com.serinity.sleepcontrol.model.Reve;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le modèle Reve
 */
@DisplayName("Tests unitaires - Reve")
class ReveTest {

    @Test
    @DisplayName("Rêve - Création et propriétés")
    void reve_CreationEtProprietes() {
        Reve reve = new Reve();
        reve.setId(1);
        reve.setTitre("Mon rêve");
        reve.setDescription("Description test");
        reve.setTypeReve("Normal");
        reve.setIntensite(5);
        reve.setHumeur("Calme");
        reve.setCouleur(true);
        reve.setRecurrent(false);

        assertEquals(1, reve.getId());
        assertEquals("Mon rêve", reve.getTitre());
        assertEquals("Description test", reve.getDescription());
        assertEquals("Normal", reve.getTypeReve());
        assertEquals(5, reve.getIntensite());
        assertEquals("Calme", reve.getHumeur());
        assertTrue(reve.isCouleur());
        assertFalse(reve.isRecurrent());
    }

    @Test
    @DisplayName("Rêve - Calcul anxiété faible")
    void reve_AnxieteFaible() {
        Reve reve = new Reve();
        reve.setTypeReve("Normal");
        reve.setIntensite(3);
        reve.setHumeur("Calme");

        int anxiete = reve.calculerNiveauAnxiete();

        assertTrue(anxiete >= 0 && anxiete <= 4);
    }

    @Test
    @DisplayName("Rêve - Calcul anxiété moyenne")
    void reve_AnxieteMoyenne() {
        Reve reve = new Reve();
        reve.setTypeReve("Normal");
        reve.setIntensite(6);
        reve.setHumeur("Triste");

        int anxiete = reve.calculerNiveauAnxiete();

        assertTrue(anxiete >= 2 && anxiete < 7);
    }

    @Test
    @DisplayName("Rêve - Calcul anxiété élevée")
    void reve_AnxieteElevee() {
        Reve reve = new Reve();
        reve.setTypeReve("Cauchemar");
        reve.setIntensite(9);
        reve.setHumeur("Anxieux");

        int anxiete = reve.calculerNiveauAnxiete();

        assertTrue(anxiete >= 7);
    }

    @Test
    @DisplayName("Rêve - Détection cauchemar")
    void reve_EstCauchemar() {
        Reve reve = new Reve();
        reve.setTypeReve("Cauchemar");

        assertTrue(reve.estCauchemar());
    }

    @Test
    @DisplayName("Rêve - N'est pas un cauchemar")
    void reve_NestPasCauchemar() {
        Reve reve = new Reve();
        reve.setTypeReve("Normal");

        assertFalse(reve.estCauchemar());
    }

    @Test
    @DisplayName("Rêve - Détection rêve lucide")
    void reve_EstLucide() {
        Reve reve = new Reve();
        reve.setTypeReve("Lucide");

        assertTrue(reve.estLucide());
    }

    @Test
    @DisplayName("Rêve - N'est pas lucide")
    void reve_NestPasLucide() {
        Reve reve = new Reve();
        reve.setTypeReve("Normal");

        assertFalse(reve.estLucide());
    }

    @Test
    @DisplayName("Rêve - Intensité minimale (1)")
    void reve_IntensiteMin() {
        Reve reve = new Reve();
        reve.setIntensite(1);

        assertEquals(1, reve.getIntensite());
    }

    @Test
    @DisplayName("Rêve - Intensité maximale (10)")
    void reve_IntensiteMax() {
        Reve reve = new Reve();
        reve.setIntensite(10);

        assertEquals(10, reve.getIntensite());
    }

    @Test
    @DisplayName("Rêve - Intensité invalide lance une exception")
    void reve_IntensiteInvalide() {
        Reve reve = new Reve();

        assertThrows(IllegalArgumentException.class, () -> {
            reve.setIntensite(0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            reve.setIntensite(11);
        });
    }

    @Test
    @DisplayName("Rêve - En couleur")
    void reve_EnCouleur() {
        Reve reve = new Reve();
        reve.setCouleur(true);

        assertTrue(reve.isCouleur());
    }

    @Test
    @DisplayName("Rêve - En noir et blanc")
    void reve_NoirEtBlanc() {
        Reve reve = new Reve();
        reve.setCouleur(false);

        assertFalse(reve.isCouleur());
    }

    @Test
    @DisplayName("Rêve - Récurrent activé")
    void reve_RecurrentActive() {
        Reve reve = new Reve();
        reve.setRecurrent(true);

        assertTrue(reve.isRecurrent());
    }

    @Test
    @DisplayName("Rêve - Récurrent désactivé")
    void reve_RecurrentDesactive() {
        Reve reve = new Reve();
        reve.setRecurrent(false);

        assertFalse(reve.isRecurrent());
    }

    @Test
    @DisplayName("Rêve - Ajouter une émotion")
    void reve_AjouterEmotion() {
        Reve reve = new Reve();
        reve.ajouterEmotion("Joie");

        assertEquals("Joie", reve.getEmotions());
    }

    @Test
    @DisplayName("Rêve - Ajouter plusieurs émotions")
    void reve_AjouterPlusieursEmotions() {
        Reve reve = new Reve();
        reve.ajouterEmotion("Joie");
        reve.ajouterEmotion("Surprise");
        reve.ajouterEmotion("Peur");

        assertTrue(reve.getEmotions().contains("Joie"));
        assertTrue(reve.getEmotions().contains("Surprise"));
        assertTrue(reve.getEmotions().contains("Peur"));
    }

    @Test
    @DisplayName("Rêve - Éviter doublons d'émotions")
    void reve_EviterDoublonsEmotions() {
        Reve reve = new Reve();
        reve.ajouterEmotion("Joie");
        reve.ajouterEmotion("Joie");

        List<String> emotions = reve.getEmotionsList();
        assertEquals(1, emotions.size());
    }

    @Test
    @DisplayName("Rêve - Liste émotions vide")
    void reve_ListeEmotionsVide() {
        Reve reve = new Reve();

        List<String> emotions = reve.getEmotionsList();
        assertTrue(emotions.isEmpty());
    }

    @Test
    @DisplayName("Rêve - Ajouter un symbole")
    void reve_AjouterSymbole() {
        Reve reve = new Reve();
        reve.ajouterSymbole("Eau");

        assertEquals("Eau", reve.getSymboles());
    }

    @Test
    @DisplayName("Rêve - Ajouter plusieurs symboles")
    void reve_AjouterPlusieursSymboles() {
        Reve reve = new Reve();
        reve.ajouterSymbole("Eau");
        reve.ajouterSymbole("Feu");
        reve.ajouterSymbole("Oiseau");

        assertTrue(reve.getSymboles().contains("Eau"));
        assertTrue(reve.getSymboles().contains("Feu"));
        assertTrue(reve.getSymboles().contains("Oiseau"));
    }

    @Test
    @DisplayName("Rêve - Liste symboles vide")
    void reve_ListeSymbolesVide() {
        Reve reve = new Reve();

        List<String> symboles = reve.getSymbolesList();
        assertTrue(symboles.isEmpty());
    }

    @Test
    @DisplayName("Rêve - Générer résumé")
    void reve_GenererResume() {
        Reve reve = new Reve();
        reve.setTitre("Rêve de vol");
        reve.setTypeReve("Normal");

        String resume = reve.genererResume();

        assertTrue(resume.contains("Rêve de vol"));
        assertTrue(resume.contains("Normal"));
    }

    @Test
    @DisplayName("Rêve - Résumé avec emoji cauchemar")
    void reve_ResumeAvecEmojiCauchemar() {
        Reve reve = new Reve();
        reve.setTitre("Cauchemar terrible");
        reve.setTypeReve("Cauchemar");

        String resume = reve.genererResume();

        assertTrue(resume.contains("⚠️"));
    }

    @Test
    @DisplayName("Rêve - Résumé avec emoji lucide")
    void reve_ResumeAvecEmojiLucide() {
        Reve reve = new Reve();
        reve.setTitre("Rêve lucide");
        reve.setTypeReve("Lucide");

        String resume = reve.genererResume();

        assertTrue(resume.contains("✨"));
    }

    @Test
    @DisplayName("Rêve - Générer rapport détaillé")
    void reve_GenererRapportDetaille() {
        Reve reve = new Reve();
        reve.setTitre("Test");
        reve.setDescription("Description");
        reve.setTypeReve("Normal");
        reve.setIntensite(5);
        reve.setHumeur("Calme");

        String rapport = reve.genererRapportDetaille();

        assertNotNull(rapport);
        assertTrue(rapport.contains("Test"));
        assertTrue(rapport.contains("Normal"));
    }

    @Test
    @DisplayName("Rêve - Validation rêve valide")
    void reve_EstValide() {
        Reve reve = new Reve();
        reve.setTitre("Titre valide");
        reve.setDescription("Description valide");
        reve.setTypeReve("Normal");
        reve.setIntensite(5);

        assertTrue(reve.estValide());
    }

    @Test
    @DisplayName("Rêve - Validation sans titre")
    void reve_InvalideSansTitre() {
        Reve reve = new Reve();
        reve.setDescription("Description");
        reve.setTypeReve("Normal");
        reve.setIntensite(5);

        assertFalse(reve.estValide());
    }

    @Test
    @DisplayName("Rêve - Validation sans description")
    void reve_InvalideSansDescription() {
        Reve reve = new Reve();
        reve.setTitre("Titre");
        reve.setTypeReve("Normal");
        reve.setIntensite(5);

        assertFalse(reve.estValide());
    }

    @Test
    @DisplayName("Rêve - Validation sans type")
    void reve_InvalideSansType() {
        Reve reve = new Reve();
        reve.setTitre("Titre");
        reve.setDescription("Description");
        reve.setIntensite(5);

        assertFalse(reve.estValide());
    }

    @Test
    @DisplayName("Rêve - Constructeur avec paramètres")
    void reve_ConstructeurAvecParametres() {
        Reve reve = new Reve("Titre", "Description", "Calme", "Normal");

        assertEquals("Titre", reve.getTitre());
        assertEquals("Description", reve.getDescription());
        assertEquals("Calme", reve.getHumeur());
        assertEquals("Normal", reve.getTypeReve());
    }

    @Test
    @DisplayName("Rêve - Valeurs par défaut du constructeur")
    void reve_ValeursParDefaut() {
        Reve reve = new Reve();

        assertTrue(reve.isCouleur());
        assertFalse(reve.isRecurrent());
        assertEquals(5, reve.getIntensite());
        assertNotNull(reve.getCreatedAt());
    }

    @Test
    @DisplayName("Rêve complet - Toutes les propriétés")
    void reve_Complet() {
        Reve reve = new Reve();
        reve.setId(1);
        reve.setTitre("Rêve complet");
        reve.setDescription("Une longue description détaillée");
        reve.setTypeReve("Lucide");
        reve.setIntensite(8);
        reve.setHumeur("Joyeux");
        reve.setCouleur(true);
        reve.setRecurrent(false);
        reve.setEmotions("Joie, Surprise");
        reve.setSymboles("Eau, Ciel");

        assertNotNull(reve);
        assertEquals(1, reve.getId());
        assertNotNull(reve.getTitre());
        assertNotNull(reve.getDescription());
        assertNotNull(reve.getTypeReve());
        assertTrue(reve.getIntensite() > 0);
        assertNotNull(reve.getHumeur());
        assertTrue(reve.isCouleur());
        assertFalse(reve.isRecurrent());
        assertNotNull(reve.getEmotions());
        assertNotNull(reve.getSymboles());
        assertTrue(reve.estValide());
    }
}
