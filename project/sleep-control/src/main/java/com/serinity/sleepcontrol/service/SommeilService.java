package com.serinity.sleepcontrol.service;

import com.serinity.sleepcontrol.dao.SommeilDao;
import com.serinity.sleepcontrol.dao.impl.SommeilDaoJdbc;
import com.serinity.sleepcontrol.model.Sommeil;
import com.serinity.sleepcontrol.model.Reve;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class SommeilService {

    private SommeilDao sommeilDao;
    private Connection connection;

    public SommeilService(Connection connection) {
        this.connection = connection;
        this.sommeilDao = new SommeilDaoJdbc(connection);
    }

    public SommeilService(SommeilDao sommeilDao) {
        this.sommeilDao = sommeilDao;
    }

    public void creer(Sommeil sommeil) throws SQLException {
        if (sommeil == null) {
            throw new IllegalArgumentException("Le sommeil ne peut pas être null");
        }

        if (sommeil.getDateNuit() == null) {
            throw new IllegalArgumentException("La date de nuit est obligatoire");
        }
        if (sommeil.getHeureCoucher() == null || sommeil.getHeureReveil() == null) {
            throw new IllegalArgumentException("Les heures de coucher et réveil sont obligatoires");
        }

        sommeil.setDureeSommeil(sommeil.calculerDuree());
        sommeilDao.ajouter(sommeil);
    }

    public List<Sommeil> listerTous() throws SQLException {
        return sommeilDao.listerTous();
    }

    public Sommeil trouverParId(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("L'ID doit être positif");
        }
        return sommeilDao.trouverParId(id);
    }

    public void modifier(Sommeil sommeil) throws SQLException {
        if (sommeil == null || sommeil.getId() <= 0) {
            throw new IllegalArgumentException("Sommeil invalide pour modification");
        }

        sommeil.setDureeSommeil(sommeil.calculerDuree());
        sommeilDao.modifier(sommeil);
    }

    public void supprimer(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("L'ID doit être positif");
        }
        sommeilDao.supprimer(id);
    }

    public List<Sommeil> rechercherDynamique(String critere) throws SQLException {
        if (critere == null || critere.trim().isEmpty()) {
            return listerTous();
        }
        return sommeilDao.rechercher(critere.trim());
    }

    public List<Sommeil> rechercherAvancee(String qualite, LocalDate dateDebut,
                                           LocalDate dateFin, Double dureeMin,
                                           Double dureeMax) throws SQLException {
        List<Sommeil> resultats = listerTous();

        if (qualite != null && !qualite.isEmpty()) {
            resultats = resultats.stream()
                    .filter(s -> s.getQualite().equalsIgnoreCase(qualite))
                    .collect(Collectors.toList());
        }

        if (dateDebut != null) {
            resultats = resultats.stream()
                    .filter(s -> !s.getDateNuit().isBefore(dateDebut))
                    .collect(Collectors.toList());
        }

        if (dateFin != null) {
            resultats = resultats.stream()
                    .filter(s -> !s.getDateNuit().isAfter(dateFin))
                    .collect(Collectors.toList());
        }

        if (dureeMin != null) {
            resultats = resultats.stream()
                    .filter(s -> s.getDureeSommeil() >= dureeMin)
                    .collect(Collectors.toList());
        }

        if (dureeMax != null) {
            resultats = resultats.stream()
                    .filter(s -> s.getDureeSommeil() <= dureeMax)
                    .collect(Collectors.toList());
        }

        return resultats;
    }

    public List<Sommeil> trierParDate(boolean croissant) throws SQLException {
        List<Sommeil> sommeils = listerTous();
        Comparator<Sommeil> comparator = Comparator.comparing(Sommeil::getDateNuit);
        return sommeils.stream()
                .sorted(croissant ? comparator : comparator.reversed())
                .collect(Collectors.toList());
    }

    public List<Sommeil> trierParDuree(boolean croissant) throws SQLException {
        List<Sommeil> sommeils = listerTous();
        Comparator<Sommeil> comparator = Comparator.comparingDouble(Sommeil::getDureeSommeil);
        return sommeils.stream()
                .sorted(croissant ? comparator : comparator.reversed())
                .collect(Collectors.toList());
    }

    public List<Sommeil> trierParQualite(boolean croissant) throws SQLException {
        List<Sommeil> sommeils = listerTous();

        Map<String, Integer> ordreQualite = new HashMap<>();
        ordreQualite.put("Excellente", 4);
        ordreQualite.put("Bonne", 3);
        ordreQualite.put("Moyenne", 2);
        ordreQualite.put("Mauvaise", 1);

        Comparator<Sommeil> comparator = Comparator.comparingInt(
                s -> ordreQualite.getOrDefault(s.getQualite(), 0)
        );

        return sommeils.stream()
                .sorted(croissant ? comparator : comparator.reversed())
                .collect(Collectors.toList());
    }

    public List<Sommeil> trierParInterruptions(boolean croissant) throws SQLException {
        List<Sommeil> sommeils = listerTous();
        Comparator<Sommeil> comparator = Comparator.comparingInt(Sommeil::getInterruptions);
        return sommeils.stream()
                .sorted(croissant ? comparator : comparator.reversed())
                .collect(Collectors.toList());
    }

    public List<Sommeil> trierParScore(boolean croissant) throws SQLException {
        List<Sommeil> sommeils = listerTous();
        Comparator<Sommeil> comparator = Comparator.comparingInt(Sommeil::calculerScoreQualite);
        return sommeils.stream()
                .sorted(croissant ? comparator : comparator.reversed())
                .collect(Collectors.toList());
    }

    public List<Sommeil> filtrerParQualite(String qualite) throws SQLException {
        if (qualite == null || qualite.trim().isEmpty()) {
            return listerTous();
        }
        return sommeilDao.filtrerParQualite(qualite);
    }

    public List<Sommeil> filtrerParPeriode(LocalDate debut, LocalDate fin) throws SQLException {
        if (debut == null || fin == null) {
            throw new IllegalArgumentException("Les dates de début et fin sont obligatoires");
        }
        if (debut.isAfter(fin)) {
            throw new IllegalArgumentException("La date de début doit être avant la date de fin");
        }
        return sommeilDao.filtrerParPeriode(debut, fin);
    }

    public List<Sommeil> filtrerParDuree(double min, double max) throws SQLException {
        if (min < 0 || max < 0 || min > max) {
            throw new IllegalArgumentException("Plage de durée invalide");
        }
        return listerTous().stream()
                .filter(s -> s.getDureeSommeil() >= min && s.getDureeSommeil() <= max)
                .collect(Collectors.toList());
    }

    public List<Sommeil> filtrerParInterruptions(int maxInterruptions) throws SQLException {
        if (maxInterruptions < 0) {
            throw new IllegalArgumentException("Le nombre d'interruptions ne peut pas être négatif");
        }
        return listerTous().stream()
                .filter(s -> s.getInterruptions() <= maxInterruptions)
                .collect(Collectors.toList());
    }

    public List<Sommeil> filtrerParHumeur(String humeur) throws SQLException {
        if (humeur == null || humeur.trim().isEmpty()) {
            return listerTous();
        }
        return listerTous().stream()
                .filter(s -> humeur.equalsIgnoreCase(s.getHumeurReveil()))
                .collect(Collectors.toList());
    }

    public List<Sommeil> filtrerParEnvironnement(String environnement) throws SQLException {
        if (environnement == null || environnement.trim().isEmpty()) {
            return listerTous();
        }
        return listerTous().stream()
                .filter(s -> environnement.equalsIgnoreCase(s.getEnvironnement()))
                .collect(Collectors.toList());
    }

    public List<Sommeil> filtrerDureeOptimale() throws SQLException {
        return listerTous().stream()
                .filter(Sommeil::estDureeOptimale)
                .collect(Collectors.toList());
    }

    public double calculerDureeMoyenne() throws SQLException {
        return sommeilDao.calculerDureeMoyenne();
    }

    public List<Object[]> obtenirStatistiquesParQualite() throws SQLException {
        return sommeilDao.statistiquesParQualite();
    }

    public double calculerMoyenneInterruptions() throws SQLException {
        List<Sommeil> sommeils = listerTous();
        if (sommeils.isEmpty()) return 0;

        return sommeils.stream()
                .mapToInt(Sommeil::getInterruptions)
                .average()
                .orElse(0);
    }

    public double calculerScoreMoyen() throws SQLException {
        List<Sommeil> sommeils = listerTous();
        if (sommeils.isEmpty()) return 0;

        return sommeils.stream()
                .mapToInt(Sommeil::calculerScoreQualite)
                .average()
                .orElse(0);
    }

    public Map<String, Object> statistiquesSemaine() throws SQLException {
        LocalDate fin = LocalDate.now();
        LocalDate debut = fin.minusDays(7);

        List<Sommeil> sommeils = filtrerParPeriode(debut, fin);

        Map<String, Object> stats = new HashMap<>();
        stats.put("nombreNuits", sommeils.size());
        stats.put("dureeMoyenne", sommeils.stream()
                .mapToDouble(Sommeil::getDureeSommeil)
                .average()
                .orElse(0));
        stats.put("interruptionsMoyennes", sommeils.stream()
                .mapToInt(Sommeil::getInterruptions)
                .average()
                .orElse(0));
        stats.put("scoreMoyen", sommeils.stream()
                .mapToInt(Sommeil::calculerScoreQualite)
                .average()
                .orElse(0));

        return stats;
    }

    public Map<String, Object> statistiquesMois() throws SQLException {
        LocalDate fin = LocalDate.now();
        LocalDate debut = fin.minusMonths(1);

        List<Sommeil> sommeils = filtrerParPeriode(debut, fin);

        Map<String, Object> stats = new HashMap<>();
        stats.put("nombreNuits", sommeils.size());
        stats.put("dureeMoyenne", sommeils.stream()
                .mapToDouble(Sommeil::getDureeSommeil)
                .average()
                .orElse(0));
        stats.put("meilleurScore", sommeils.stream()
                .mapToInt(Sommeil::calculerScoreQualite)
                .max()
                .orElse(0));
        stats.put("pireScore", sommeils.stream()
                .mapToInt(Sommeil::calculerScoreQualite)
                .min()
                .orElse(0));

        Map<String, Long> distribution = sommeils.stream()
                .collect(Collectors.groupingBy(Sommeil::getQualite, Collectors.counting()));
        stats.put("distributionQualite", distribution);

        return stats;
    }

    public Map<LocalDate, Double> donneesGraphiqueDuree(LocalDate debut, LocalDate fin)
            throws SQLException {
        List<Sommeil> sommeils = filtrerParPeriode(debut, fin);

        return sommeils.stream()
                .collect(Collectors.toMap(
                        Sommeil::getDateNuit,
                        Sommeil::getDureeSommeil,
                        (a, b) -> a,
                        TreeMap::new
                ));
    }

    public Map<LocalDate, Integer> donneesGraphiqueScore(LocalDate debut, LocalDate fin)
            throws SQLException {
        List<Sommeil> sommeils = filtrerParPeriode(debut, fin);

        return sommeils.stream()
                .collect(Collectors.toMap(
                        Sommeil::getDateNuit,
                        Sommeil::calculerScoreQualite,
                        (a, b) -> a,
                        TreeMap::new
                ));
    }

    public String analyserQualiteSommeil(Sommeil sommeil) {
        if (sommeil == null) return "Aucune donnée";

        StringBuilder analyse = new StringBuilder();

        double duree = sommeil.getDureeSommeil();
        if (duree < 6) {
            analyse.append("Durée insuffisante (< 6h). ");
        } else if (duree >= 7 && duree <= 9) {
            analyse.append("Durée optimale (7-9h). ");
        } else if (duree > 9) {
            analyse.append("Durée excessive (> 9h). ");
        } else {
            analyse.append("Durée acceptable (6-7h). ");
        }

        int interruptions = sommeil.getInterruptions();
        if (interruptions == 0) {
            analyse.append("Sommeil continu. ");
        } else if (interruptions <= 2) {
            analyse.append("Quelques interruptions. ");
        } else {
            analyse.append("Trop d'interruptions (" + interruptions + "). ");
        }

        int score = sommeil.calculerScoreQualite();
        if (score >= 80) {
            analyse.append("Excellente nuit (score: " + score + "/100).");
        } else if (score >= 60) {
            analyse.append("Bonne nuit (score: " + score + "/100).");
        } else if (score >= 40) {
            analyse.append("Nuit moyenne (score: " + score + "/100).");
        } else {
            analyse.append("Nuit difficile (score: " + score + "/100).");
        }

        return analyse.toString();
    }

    public List<String> genererRecommandations() throws SQLException {
        List<String> recommandations = new ArrayList<>();

        List<Sommeil> sommeils = listerTous();
        if (sommeils.isEmpty()) {
            recommandations.add("Commencez à enregistrer vos nuits de sommeil");
            return recommandations;
        }

        double dureeMoyenne = calculerDureeMoyenne();
        double interruptionsMoyennes = calculerMoyenneInterruptions();

        if (dureeMoyenne < 7) {
            recommandations.add("Essayez de dormir au moins 7 heures par nuit");
            recommandations.add("Établissez une heure de coucher régulière");
        } else if (dureeMoyenne > 9) {
            recommandations.add("Une durée de sommeil excessive peut indiquer un problème");
        }

        if (interruptionsMoyennes > 2) {
            recommandations.add("Réduisez les sources de bruit dans votre chambre");
            recommandations.add("Maintenez une température confortable (18-20°C)");
            recommandations.add("Évitez les écrans 1h avant le coucher");
        }

        if (sommeils.size() >= 7) {
            List<Sommeil> derniereSemaine = sommeils.stream()
                    .limit(7)
                    .collect(Collectors.toList());

            double ecartType = calculerEcartTypeDuree(derniereSemaine);
            if (ecartType > 1.5) {
                recommandations.add("Essayez de maintenir des horaires réguliers");
            }
        }

        if (recommandations.isEmpty()) {
            recommandations.add("Votre sommeil est de bonne qualité, continuez ainsi!");
        }

        return recommandations;
    }

    private double calculerEcartTypeDuree(List<Sommeil> sommeils) {
        if (sommeils.isEmpty()) return 0;

        double moyenne = sommeils.stream()
                .mapToDouble(Sommeil::getDureeSommeil)
                .average()
                .orElse(0);

        double variance = sommeils.stream()
                .mapToDouble(s -> Math.pow(s.getDureeSommeil() - moyenne, 2))
                .average()
                .orElse(0);

        return Math.sqrt(variance);
    }

    public Map<String, String> identifierTendances(LocalDate debut, LocalDate fin)
            throws SQLException {
        List<Sommeil> sommeils = filtrerParPeriode(debut, fin);
        Map<String, String> tendances = new HashMap<>();

        if (sommeils.size() < 3) {
            tendances.put("message", "Pas assez de données pour identifier des tendances");
            return tendances;
        }

        List<Double> durees = sommeils.stream()
                .map(Sommeil::getDureeSommeil)
                .collect(Collectors.toList());

        if (estCroissant(durees)) {
            tendances.put("duree", "Amélioration de la durée de sommeil");
        } else if (estDecroissant(durees)) {
            tendances.put("duree", "Diminution de la durée de sommeil");
        } else {
            tendances.put("duree", "Durée stable");
        }

        List<Integer> scores = sommeils.stream()
                .map(Sommeil::calculerScoreQualite)
                .collect(Collectors.toList());

        if (estCroissant(scores.stream().map(Double::valueOf).collect(Collectors.toList()))) {
            tendances.put("qualite", "Amélioration de la qualité");
        } else if (estDecroissant(scores.stream().map(Double::valueOf).collect(Collectors.toList()))) {
            tendances.put("qualite", "Détérioration de la qualité");
        } else {
            tendances.put("qualite", "Qualité stable");
        }

        return tendances;
    }

    private boolean estCroissant(List<Double> valeurs) {
        if (valeurs.size() < 2) return false;

        int augmentations = 0;
        for (int i = 1; i < valeurs.size(); i++) {
            if (valeurs.get(i) > valeurs.get(i-1)) augmentations++;
        }

        return augmentations > valeurs.size() / 2;
    }

    private boolean estDecroissant(List<Double> valeurs) {
        if (valeurs.size() < 2) return false;

        int diminutions = 0;
        for (int i = 1; i < valeurs.size(); i++) {
            if (valeurs.get(i) < valeurs.get(i-1)) diminutions++;
        }

        return diminutions > valeurs.size() / 2;
    }

    public Sommeil trouverMeilleurSommeil() throws SQLException {
        return listerTous().stream()
                .max(Comparator.comparingInt(Sommeil::calculerScoreQualite))
                .orElse(null);
    }

    public Sommeil trouverPireSommeil() throws SQLException {
        return listerTous().stream()
                .min(Comparator.comparingInt(Sommeil::calculerScoreQualite))
                .orElse(null);
    }

    public int compterSommeils(LocalDate debut, LocalDate fin) throws SQLException {
        return filtrerParPeriode(debut, fin).size();
    }

    public boolean existePourDate(LocalDate date) throws SQLException {
        return listerTous().stream()
                .anyMatch(s -> s.getDateNuit().equals(date));
    }

    public List<Sommeil> listerTousAvecReves() throws SQLException {
        List<Sommeil> sommeils = listerTous();

        for (Sommeil sommeil : sommeils) {
            chargerRevesAssocies(sommeil);
        }

        return sommeils;
    }

    public Sommeil trouverParIdAvecReves(int id) throws SQLException {
        Sommeil sommeil = trouverParId(id);

        if (sommeil != null) {
            chargerRevesAssocies(sommeil);
        }

        return sommeil;
    }

    private void chargerRevesAssocies(Sommeil sommeil) throws SQLException {
        if (sommeil == null) return;

        String sql = "SELECT * FROM reves WHERE sommeil_id = ? ORDER BY id DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, sommeil.getId());
            ResultSet rs = ps.executeQuery();

            List<Reve> reves = new ArrayList<>();

            while (rs.next()) {
                Reve reve = new Reve();
                reve.setId(rs.getInt("id"));
                reve.setSommeilId(rs.getInt("sommeil_id"));
                reve.setTitre(rs.getString("titre"));
                reve.setDescription(rs.getString("description"));
                reve.setTypeReve(rs.getString("type_reve"));
                reve.setHumeur(rs.getString("humeur"));
                reve.setIntensite(rs.getInt("intensite"));
                reve.setCouleur(rs.getBoolean("couleur"));
                reve.setRecurrent(rs.getBoolean("recurrent"));
                reve.setEmotions(rs.getString("emotions"));
                reve.setSymboles(rs.getString("symboles"));

                reve.setSommeil(sommeil);
                reves.add(reve);
            }

            sommeil.setReves(reves);
            rs.close();
        }
    }
}
