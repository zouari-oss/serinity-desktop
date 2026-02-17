package com.serinity.sleepcontrol.service;

import com.serinity.sleepcontrol.dao.ReveDao;
import com.serinity.sleepcontrol.dao.impl.ReveDaoJdbc;
import com.serinity.sleepcontrol.model.Reve;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service métier pour la gestion des rêves
 * Contient toutes les opérations CRUD et métiers avancés
 */
public class ReveService {

    private ReveDao reveDao;

    // ==================== CONSTRUCTEUR ====================

    public ReveService(Connection connection) {
        this.reveDao = new ReveDaoJdbc(connection);
    }

    // Pour les tests unitaires (injection de dépendance)
    public ReveService(ReveDao reveDao) {
        this.reveDao = reveDao;
    }

    // ==================== CRUD DE BASE ====================

    /**
     * Crée un nouveau rêve dans la base de données
     */
    public void creer(Reve reve) throws SQLException {
        if (reve == null) {
            throw new IllegalArgumentException("Le rêve ne peut pas être null");
        }

        if (!reve.estValide()) {
            throw new IllegalArgumentException("Les données du rêve sont invalides");
        }

        if (reve.getSommeilId() <= 0) {
            throw new IllegalArgumentException("Le rêve doit être associé à un sommeil");
        }

        reveDao.ajouter(reve);
    }

    /**
     * Liste tous les rêves
     */
    public List<Reve> listerTous() throws SQLException {
        return reveDao.listerTous();
    }

    /**
     * Trouve un rêve par son ID
     */
    public Reve trouverParId(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("L'ID doit être positif");
        }
        return reveDao.trouverParId(id);
    }

    /**
     * Trouve tous les rêves d'un sommeil spécifique
     */
    public List<Reve> trouverParSommeilId(int sommeilId) throws SQLException {
        if (sommeilId <= 0) {
            throw new IllegalArgumentException("L'ID du sommeil doit être positif");
        }
        return reveDao.trouverParSommeilId(sommeilId);
    }

    /**
     * Modifie un rêve existant
     */
    public void modifier(Reve reve) throws SQLException {
        if (reve == null || reve.getId() <= 0) {
            throw new IllegalArgumentException("Rêve invalide pour modification");
        }

        if (!reve.estValide()) {
            throw new IllegalArgumentException("Les données du rêve sont invalides");
        }

        reveDao.modifier(reve);
    }

    /**
     * Supprime un rêve
     */
    public void supprimer(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("L'ID doit être positif");
        }
        reveDao.supprimer(id);
    }

    // ==================== RECHERCHE DYNAMIQUE ====================

    /**
     * Recherche dynamique par critère
     * Recherche dans titre, description, émotions et symboles
     */
    public List<Reve> rechercherDynamique(String critere) throws SQLException {
        if (critere == null || critere.trim().isEmpty()) {
            return listerTous();
        }
        return reveDao.rechercher(critere.trim());
    }

    /**
     * Recherche avancée avec plusieurs critères
     */
    public List<Reve> rechercherAvancee(String type, String humeur, Integer intensiteMin,
                                        Integer intensiteMax, Boolean recurrent)
            throws SQLException {
        List<Reve> resultats = listerTous();

        if (type != null && !type.isEmpty()) {
            resultats = resultats.stream()
                    .filter(r -> r.getTypeReve().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        if (humeur != null && !humeur.isEmpty()) {
            resultats = resultats.stream()
                    .filter(r -> humeur.equalsIgnoreCase(r.getHumeur()))
                    .collect(Collectors.toList());
        }

        if (intensiteMin != null) {
            resultats = resultats.stream()
                    .filter(r -> r.getIntensite() >= intensiteMin)
                    .collect(Collectors.toList());
        }

        if (intensiteMax != null) {
            resultats = resultats.stream()
                    .filter(r -> r.getIntensite() <= intensiteMax)
                    .collect(Collectors.toList());
        }

        if (recurrent != null) {
            resultats = resultats.stream()
                    .filter(r -> r.isRecurrent() == recurrent)
                    .collect(Collectors.toList());
        }

        return resultats;
    }

    // ==================== TRI ====================

    /**
     * Trie les rêves par intensité
     */
    public List<Reve> trierParIntensite(boolean croissant) throws SQLException {
        List<Reve> reves = listerTous();
        Comparator<Reve> comparator = Comparator.comparingInt(Reve::getIntensite);
        return reves.stream()
                .sorted(croissant ? comparator : comparator.reversed())
                .collect(Collectors.toList());
    }

    /**
     * Trie les rêves par titre (ordre alphabétique)
     */
    public List<Reve> trierParTitre(boolean croissant) throws SQLException {
        List<Reve> reves = listerTous();
        Comparator<Reve> comparator = Comparator.comparing(Reve::getTitre);
        return reves.stream()
                .sorted(croissant ? comparator : comparator.reversed())
                .collect(Collectors.toList());
    }

    /**
     * Trie par type de rêve
     */
    public List<Reve> trierParType(boolean croissant) throws SQLException {
        List<Reve> reves = listerTous();

        Map<String, Integer> ordreType = new HashMap<>();
        ordreType.put("Lucide", 4);
        ordreType.put("Normal", 3);
        ordreType.put("Récurrent", 2);
        ordreType.put("Cauchemar", 1);

        Comparator<Reve> comparator = Comparator.comparingInt(
                r -> ordreType.getOrDefault(r.getTypeReve(), 0)
        );

        return reves.stream()
                .sorted(croissant ? comparator : comparator.reversed())
                .collect(Collectors.toList());
    }

    /**
     * Trie par niveau d'anxiété
     */
    public List<Reve> trierParAnxiete(boolean croissant) throws SQLException {
        List<Reve> reves = listerTous();
        Comparator<Reve> comparator = Comparator.comparingInt(Reve::calculerNiveauAnxiete);
        return reves.stream()
                .sorted(croissant ? comparator : comparator.reversed())
                .collect(Collectors.toList());
    }

    // ==================== FILTRES ====================

    /**
     * Filtre par type spécifique
     */
    public List<Reve> filtrerParType(String type) throws SQLException {
        if (type == null || type.trim().isEmpty()) {
            return listerTous();
        }
        return reveDao.filtrerParType(type);
    }

    /**
     * Filtre par plage d'intensité
     */
    public List<Reve> filtrerParIntensite(int min, int max) throws SQLException {
        if (min < 1 || max > 10 || min > max) {
            throw new IllegalArgumentException("Plage d'intensité invalide (1-10)");
        }
        return listerTous().stream()
                .filter(r -> r.getIntensite() >= min && r.getIntensite() <= max)
                .collect(Collectors.toList());
    }

    /**
     * Filtre les rêves récurrents uniquement
     */
    public List<Reve> filtrerRecurrents() throws SQLException {
        return listerTous().stream()
                .filter(Reve::isRecurrent)
                .collect(Collectors.toList());
    }

    /**
     * Filtre les cauchemars
     */
    public List<Reve> filtrerCauchemars() throws SQLException {
        return listerTous().stream()
                .filter(Reve::estCauchemar)
                .collect(Collectors.toList());
    }

    /**
     * Filtre les rêves lucides
     */
    public List<Reve> filtrerLucides() throws SQLException {
        return listerTous().stream()
                .filter(Reve::estLucide)
                .collect(Collectors.toList());
    }

    /**
     * Filtre par humeur
     */
    public List<Reve> filtrerParHumeur(String humeur) throws SQLException {
        if (humeur == null || humeur.trim().isEmpty()) {
            return listerTous();
        }
        return listerTous().stream()
                .filter(r -> humeur.equalsIgnoreCase(r.getHumeur()))
                .collect(Collectors.toList());
    }

    /**
     * Filtre les rêves en couleur
     */
    public List<Reve> filtrerEnCouleur(boolean couleur) throws SQLException {
        return listerTous().stream()
                .filter(r -> r.isCouleur() == couleur)
                .collect(Collectors.toList());
    }

    /**
     * Filtre par niveau d'anxiété minimum
     */
    public List<Reve> filtrerParNiveauAnxiete(int niveauMin) throws SQLException {
        return listerTous().stream()
                .filter(r -> r.calculerNiveauAnxiete() >= niveauMin)
                .collect(Collectors.toList());
    }

    // ==================== STATISTIQUES ====================

    /**
     * Obtient les statistiques par type
     * Retourne: [type, nombre, intensité moyenne]
     */
    public List<Object[]> obtenirStatistiquesParType() throws SQLException {
        return reveDao.statistiquesParType();
    }

    /**
     * Calcule l'intensité moyenne de tous les rêves
     */
    public double calculerIntensiteMoyenne() throws SQLException {
        List<Reve> reves = listerTous();
        if (reves.isEmpty()) return 0;

        return reves.stream()
                .mapToInt(Reve::getIntensite)
                .average()
                .orElse(0);
    }

    /**
     * Calcule le niveau d'anxiété moyen
     */
    public double calculerAnxieteMoyenne() throws SQLException {
        List<Reve> reves = listerTous();
        if (reves.isEmpty()) return 0;

        return reves.stream()
                .mapToInt(Reve::calculerNiveauAnxiete)
                .average()
                .orElse(0);
    }

    /**
     * Compte les rêves par type
     */
    public Map<String, Long> compterParType() throws SQLException {
        return listerTous().stream()
                .collect(Collectors.groupingBy(Reve::getTypeReve, Collectors.counting()));
    }

    /**
     * Compte les rêves par humeur
     */
    public Map<String, Long> compterParHumeur() throws SQLException {
        return listerTous().stream()
                .filter(r -> r.getHumeur() != null)
                .collect(Collectors.groupingBy(Reve::getHumeur, Collectors.counting()));
    }

    /**
     * Calcule le pourcentage de cauchemars
     */
    public double calculerPourcentageCauchemars() throws SQLException {
        List<Reve> reves = listerTous();
        if (reves.isEmpty()) return 0;

        long nbCauchemars = reves.stream().filter(Reve::estCauchemar).count();
        return (nbCauchemars * 100.0) / reves.size();
    }

    /**
     * Calcule le pourcentage de rêves récurrents
     */
    public double calculerPourcentageRecurrents() throws SQLException {
        List<Reve> reves = listerTous();
        if (reves.isEmpty()) return 0;

        long nbRecurrents = reves.stream().filter(Reve::isRecurrent).count();
        return (nbRecurrents * 100.0) / reves.size();
    }

    /**
     * Obtient les émotions les plus fréquentes
     */
    public Map<String, Long> emotionsFrequentes() throws SQLException {
        return listerTous().stream()
                .flatMap(r -> r.getEmotionsList().stream())
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    /**
     * Obtient les symboles les plus fréquents
     */
    public Map<String, Long> symbolesFrequents() throws SQLException {
        return listerTous().stream()
                .flatMap(r -> r.getSymbolesList().stream())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    /**
     * Statistiques détaillées globales
     */
    public Map<String, Object> statistiquesGlobales() throws SQLException {
        List<Reve> reves = listerTous();
        Map<String, Object> stats = new HashMap<>();

        stats.put("nombreTotal", reves.size());
        stats.put("intensiteMoyenne", calculerIntensiteMoyenne());
        stats.put("anxieteMoyenne", calculerAnxieteMoyenne());
        stats.put("pourcentageCauchemars", calculerPourcentageCauchemars());
        stats.put("pourcentageRecurrents", calculerPourcentageRecurrents());
        stats.put("pourcentageCouleur", calculerPourcentageEnCouleur());
        stats.put("repartitionTypes", compterParType());

        return stats;
    }

    private double calculerPourcentageEnCouleur() throws SQLException {
        List<Reve> reves = listerTous();
        if (reves.isEmpty()) return 0;

        long nbCouleur = reves.stream().filter(Reve::isCouleur).count();
        return (nbCouleur * 100.0) / reves.size();
    }

    // ==================== ANALYSE ====================

    /**
     * Analyse un rêve spécifique
     */
    public String analyserReve(Reve reve) {
        if (reve == null) return "Aucune donnée";

        StringBuilder analyse = new StringBuilder();

        // Type
        if (reve.estCauchemar()) {
            analyse.append("⚠️ Cauchemar détecté. ");
        } else if (reve.estLucide()) {
            analyse.append("✨ Rêve lucide (contrôle conscient). ");
        } else {
            analyse.append("😴 Rêve normal. ");
        }

        // Intensité
        int intensite = reve.getIntensite();
        if (intensite >= 8) {
            analyse.append("🔥 Très intense. ");
        } else if (intensite >= 6) {
            analyse.append("⚡ Intensité modérée. ");
        } else {
            analyse.append("💤 Faible intensité. ");
        }

        // Anxiété
        int anxiete = reve.calculerNiveauAnxiete();
        if (anxiete >= 7) {
            analyse.append("😰 Niveau d'anxiété élevé. ");
        } else if (anxiete >= 4) {
            analyse.append("😐 Anxiété modérée. ");
        } else {
            analyse.append("😌 Peu d'anxiété. ");
        }

        // Récurrence
        if (reve.isRecurrent()) {
            analyse.append("🔄 Rêve récurrent (peut avoir une signification particulière). ");
        }

        return analyse.toString();
    }

    /**
     * Génère des recommandations basées sur les rêves
     */
    public List<String> genererRecommandations() throws SQLException {
        List<String> recommandations = new ArrayList<>();

        List<Reve> reves = listerTous();
        if (reves.isEmpty()) {
            recommandations.add("Commencez à enregistrer vos rêves pour obtenir des recommandations");
            return recommandations;
        }

        double pourcentageCauchemars = calculerPourcentageCauchemars();
        double anxieteMoyenne = calculerAnxieteMoyenne();

        // Recommandations cauchemars
        if (pourcentageCauchemars > 30) {
            recommandations.add("⚠️ Taux de cauchemars élevé - envisagez des techniques de relaxation");
            recommandations.add("🧘 Pratiquez la méditation avant le coucher");
            recommandations.add("📖 Tenez un journal de gratitude");
        }

        // Recommandations anxiété
        if (anxieteMoyenne > 6) {
            recommandations.add("😰 Niveau d'anxiété élevé dans vos rêves");
            recommandations.add("💆 Consultez un spécialiste si l'anxiété persiste");
            recommandations.add("🎵 Essayez la musique relaxante avant de dormir");
        }

        // Rêves récurrents
        long nbRecurrents = reves.stream().filter(Reve::isRecurrent).count();
        if (nbRecurrents >= 3) {
            recommandations.add("🔄 Vous avez plusieurs rêves récurrents");
            recommandations.add("💭 Ils peuvent refléter des préoccupations non résolues");
        }

        // Rêves lucides
        long nbLucides = reves.stream().filter(Reve::estLucide).count();
        if (nbLucides > 0) {
            recommandations.add("✨ Vous avez des rêves lucides - excellente opportunité de contrôle");
            recommandations.add("📚 Explorez les techniques d'onirisme pour en faire davantage");
        }

        if (recommandations.isEmpty()) {
            recommandations.add("✅ Vos rêves semblent équilibrés, continuez votre suivi!");
        }

        return recommandations;
    }

    /**
     * Trouve le rêve le plus intense
     */
    public Reve trouverPlusIntense() throws SQLException {
        return listerTous().stream()
                .max(Comparator.comparingInt(Reve::getIntensite))
                .orElse(null);
    }

    /**
     * Trouve le rêve le plus anxiogène
     */
    public Reve trouverPlusAnxiogene() throws SQLException {
        return listerTous().stream()
                .max(Comparator.comparingInt(Reve::calculerNiveauAnxiete))
                .orElse(null);
    }

    /**
     * Compte le nombre total de rêves
     */
    public int compterTotal() throws SQLException {
        return listerTous().size();
    }

    /**
     * Vérifie si un rêve avec ce titre existe déjà
     */
    public boolean existeParTitre(String titre) throws SQLException {
        if (titre == null || titre.trim().isEmpty()) return false;

        return listerTous().stream()
                .anyMatch(r -> titre.equalsIgnoreCase(r.getTitre()));
    }
}
