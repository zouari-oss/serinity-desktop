package com.serinity.sleepcontrol.service;

import com.serinity.sleepcontrol.dao.SommeilDao;
import com.serinity.sleepcontrol.dao.impl.SommeilDaoJdbc;
import com.serinity.sleepcontrol.model.Sommeil;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class SommeilService {

    private final SommeilDao sommeilDao;

    public SommeilService() {
        this.sommeilDao = new SommeilDaoJdbc();
    }

    public SommeilService(SommeilDao sommeilDao) {
        this.sommeilDao = sommeilDao;
    }

    // ═══════════════════════════════════════════════════════════
    //  CRUD
    // ═══════════════════════════════════════════════════════════

    public void creer(Sommeil sommeil) throws SQLException {
        if (sommeil == null)
            throw new IllegalArgumentException("Le sommeil ne peut pas être null");
        if (sommeil.getDateNuit() == null)
            throw new IllegalArgumentException("La date de nuit est obligatoire");
        if (sommeil.getHeureCoucher() == null || sommeil.getHeureReveil() == null)
            throw new IllegalArgumentException("Les heures de coucher et réveil sont obligatoires");
        sommeil.setDureeSommeil(sommeil.calculerDuree());
        sommeilDao.ajouter(sommeil);
    }

    public List<Sommeil> listerTous() throws SQLException {
        return sommeilDao.listerTous();
    }

    public Sommeil trouverParId(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("L'ID doit être positif");
        return sommeilDao.trouverParId(id);
    }

    public void modifier(Sommeil sommeil) throws SQLException {
        if (sommeil == null || sommeil.getId() <= 0)
            throw new IllegalArgumentException("Sommeil invalide pour modification");
        sommeil.setDureeSommeil(sommeil.calculerDuree());
        sommeilDao.modifier(sommeil);
    }

    public void supprimer(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("L'ID doit être positif");
        sommeilDao.supprimer(id);
    }

    public int compterTotal() throws SQLException {
        return listerTous().size();
    }

    public boolean existePourDate(LocalDate date) throws SQLException {
        return listerTous().stream()
                .anyMatch(s -> s.getDateNuit().equals(date));
    }

    public List<Sommeil> listerTousAvecReves()   throws SQLException { return listerTous(); }
    public Sommeil trouverParIdAvecReves(int id) throws SQLException { return trouverParId(id); }

    // ═══════════════════════════════════════════════════════════
    //  RÊVES ASSOCIÉS — MÉTHODE MANQUANTE
    // ═══════════════════════════════════════════════════════════

    /**
     * Compte le nombre de rêves associés à une nuit de sommeil.
     * Adapte automatiquement selon la relation :
     *   - One-to-Many  : reve.sommeil_id = sommeilId
     *   - Many-to-Many : table de jointure sommeil_reve
     */
    public int compterRevesParSommeil(int sommeilId) {
        String sql = "SELECT COUNT(*) FROM reves WHERE sommeil_id = ?";
        try (PreparedStatement ps = sommeilDao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, sommeilId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    /**
     * Charge le nombre de rêves dans chaque Sommeil de la liste.
     * Appelle cette méthode après listerTous() pour enrichir les cards.
     */
    public List<Sommeil> listerTousAvecNbReves() throws SQLException {
        List<Sommeil> sommeils = listerTous();
        sommeils.forEach(s -> s.setNbReves(compterRevesParSommeil(s.getId())));
        return sommeils;
    }

    // ═══════════════════════════════════════════════════════════
    //  RECHERCHE & FILTRES
    // ═══════════════════════════════════════════════════════════

    public List<Sommeil> rechercherDynamique(String critere) throws SQLException {
        if (critere == null || critere.trim().isEmpty()) return listerTous();
        return sommeilDao.rechercher(critere.trim());
    }

    public List<Sommeil> rechercherAvancee(String qualite, LocalDate dateDebut,
                                           LocalDate dateFin, Double dureeMin,
                                           Double dureeMax) throws SQLException {
        List<Sommeil> res = listerTous();
        if (qualite   != null && !qualite.isEmpty())
            res = res.stream().filter(s -> s.getQualite().equalsIgnoreCase(qualite)).collect(Collectors.toList());
        if (dateDebut != null)
            res = res.stream().filter(s -> !s.getDateNuit().isBefore(dateDebut)).collect(Collectors.toList());
        if (dateFin   != null)
            res = res.stream().filter(s -> !s.getDateNuit().isAfter(dateFin)).collect(Collectors.toList());
        if (dureeMin  != null)
            res = res.stream().filter(s -> s.getDureeSommeil() >= dureeMin).collect(Collectors.toList());
        if (dureeMax  != null)
            res = res.stream().filter(s -> s.getDureeSommeil() <= dureeMax).collect(Collectors.toList());
        return res;
    }

    public List<Sommeil> filtrerParQualite(String qualite) throws SQLException {
        if (qualite == null || qualite.trim().isEmpty()) return listerTous();
        return sommeilDao.filtrerParQualite(qualite);
    }

    public List<Sommeil> filtrerParPeriode(LocalDate debut, LocalDate fin) throws SQLException {
        if (debut == null || fin == null)
            throw new IllegalArgumentException("Les dates sont obligatoires");
        if (debut.isAfter(fin))
            throw new IllegalArgumentException("La date de début doit être avant la date de fin");
        return sommeilDao.filtrerParPeriode(debut, fin);
    }

    public List<Sommeil> filtrerParDuree(double min, double max) throws SQLException {
        if (min < 0 || max < 0 || min > max)
            throw new IllegalArgumentException("Plage de durée invalide");
        return listerTous().stream()
                .filter(s -> s.getDureeSommeil() >= min && s.getDureeSommeil() <= max)
                .collect(Collectors.toList());
    }

    public List<Sommeil> filtrerParInterruptions(int maxInterruptions) throws SQLException {
        if (maxInterruptions < 0)
            throw new IllegalArgumentException("Le nombre d'interruptions ne peut pas être négatif");
        return listerTous().stream()
                .filter(s -> s.getInterruptions() <= maxInterruptions)
                .collect(Collectors.toList());
    }

    public List<Sommeil> filtrerParHumeur(String humeur) throws SQLException {
        if (humeur == null || humeur.trim().isEmpty()) return listerTous();
        return listerTous().stream()
                .filter(s -> humeur.equalsIgnoreCase(s.getHumeurReveil()))
                .collect(Collectors.toList());
    }

    public List<Sommeil> filtrerParEnvironnement(String environnement) throws SQLException {
        if (environnement == null || environnement.trim().isEmpty()) return listerTous();
        return listerTous().stream()
                .filter(s -> environnement.equalsIgnoreCase(s.getEnvironnement()))
                .collect(Collectors.toList());
    }

    public List<Sommeil> filtrerDureeOptimale() throws SQLException {
        return listerTous().stream()
                .filter(Sommeil::estDureeOptimale)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  TRIS
    // ═══════════════════════════════════════════════════════════

    public List<Sommeil> trierParDate(boolean croissant) throws SQLException {
        Comparator<Sommeil> c = Comparator.comparing(Sommeil::getDateNuit);
        return listerTous().stream().sorted(croissant ? c : c.reversed()).collect(Collectors.toList());
    }

    public List<Sommeil> trierParDuree(boolean croissant) throws SQLException {
        Comparator<Sommeil> c = Comparator.comparingDouble(Sommeil::getDureeSommeil);
        return listerTous().stream().sorted(croissant ? c : c.reversed()).collect(Collectors.toList());
    }

    public List<Sommeil> trierParQualite(boolean croissant) throws SQLException {
        Map<String, Integer> ordre = Map.of(
                "Excellente", 4, "Bonne", 3, "Moyenne", 2, "Mauvaise", 1);
        Comparator<Sommeil> c = Comparator.comparingInt(s -> ordre.getOrDefault(s.getQualite(), 0));
        return listerTous().stream().sorted(croissant ? c : c.reversed()).collect(Collectors.toList());
    }

    public List<Sommeil> trierParInterruptions(boolean croissant) throws SQLException {
        Comparator<Sommeil> c = Comparator.comparingInt(Sommeil::getInterruptions);
        return listerTous().stream().sorted(croissant ? c : c.reversed()).collect(Collectors.toList());
    }

    public List<Sommeil> trierParScore(boolean croissant) throws SQLException {
        Comparator<Sommeil> c = Comparator.comparingInt(Sommeil::calculerScoreQualite);
        return listerTous().stream().sorted(croissant ? c : c.reversed()).collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  STATISTIQUES DE BASE
    // ═══════════════════════════════════════════════════════════

    public double calculerDureeMoyenne() throws SQLException {
        return sommeilDao.calculerDureeMoyenne();
    }

    public double calculerMoyenneInterruptions() throws SQLException {
        List<Sommeil> s = listerTous();
        if (s.isEmpty()) return 0;
        return s.stream().mapToInt(Sommeil::getInterruptions).average().orElse(0);
    }

    public double calculerScoreMoyen() throws SQLException {
        List<Sommeil> s = listerTous();
        if (s.isEmpty()) return 0;
        return s.stream().mapToInt(Sommeil::calculerScoreQualite).average().orElse(0);
    }

    public List<Object[]> obtenirStatistiquesParQualite() throws SQLException {
        return sommeilDao.statistiquesParQualite();
    }

    public Map<String, Long> compterParQualite() throws SQLException {
        return listerTous().stream()
                .collect(Collectors.groupingBy(Sommeil::getQualite, Collectors.counting()));
    }

    public Map<String, Long> compterParHumeur() throws SQLException {
        return listerTous().stream()
                .filter(s -> s.getHumeurReveil() != null)
                .collect(Collectors.groupingBy(Sommeil::getHumeurReveil, Collectors.counting()));
    }

    public double calculerPourcentageDureeOptimale() throws SQLException {
        List<Sommeil> s = listerTous();
        if (s.isEmpty()) return 0;
        return (s.stream().filter(Sommeil::estDureeOptimale).count() * 100.0) / s.size();
    }

    public Sommeil trouverMeilleurSommeil() throws SQLException {
        return listerTous().stream()
                .max(Comparator.comparingInt(Sommeil::calculerScoreQualite)).orElse(null);
    }

    public Sommeil trouverPireSommeil() throws SQLException {
        return listerTous().stream()
                .min(Comparator.comparingInt(Sommeil::calculerScoreQualite)).orElse(null);
    }

    public int compterSommeils(LocalDate debut, LocalDate fin) throws SQLException {
        return filtrerParPeriode(debut, fin).size();
    }

    // ═══════════════════════════════════════════════════════════
    //  STATISTIQUES TEMPORELLES
    // ═══════════════════════════════════════════════════════════

    public Map<String, Object> statistiquesSemaine() throws SQLException {
        List<Sommeil> s = filtrerParPeriode(LocalDate.now().minusDays(7), LocalDate.now());
        Map<String, Object> stats = new HashMap<>();
        stats.put("nombreNuits",           s.size());
        stats.put("dureeMoyenne",          s.stream().mapToDouble(Sommeil::getDureeSommeil).average().orElse(0));
        stats.put("interruptionsMoyennes", s.stream().mapToInt(Sommeil::getInterruptions).average().orElse(0));
        stats.put("scoreMoyen",            s.stream().mapToInt(Sommeil::calculerScoreQualite).average().orElse(0));
        return stats;
    }

    public Map<String, Object> statistiquesMois() throws SQLException {
        List<Sommeil> s = filtrerParPeriode(LocalDate.now().minusMonths(1), LocalDate.now());
        Map<String, Object> stats = new HashMap<>();
        stats.put("nombreNuits",  s.size());
        stats.put("dureeMoyenne", s.stream().mapToDouble(Sommeil::getDureeSommeil).average().orElse(0));
        stats.put("meilleurScore", s.stream().mapToInt(Sommeil::calculerScoreQualite).max().orElse(0));
        stats.put("pireScore",     s.stream().mapToInt(Sommeil::calculerScoreQualite).min().orElse(0));
        stats.put("distributionQualite",
                s.stream().collect(Collectors.groupingBy(Sommeil::getQualite, Collectors.counting())));
        return stats;
    }

    public Map<LocalDate, Double> donneesGraphiqueDuree(LocalDate debut, LocalDate fin)
            throws SQLException {
        return filtrerParPeriode(debut, fin).stream()
                .collect(Collectors.toMap(Sommeil::getDateNuit, Sommeil::getDureeSommeil,
                        (a, b) -> a, TreeMap::new));
    }

    public Map<LocalDate, Integer> donneesGraphiqueScore(LocalDate debut, LocalDate fin)
            throws SQLException {
        return filtrerParPeriode(debut, fin).stream()
                .collect(Collectors.toMap(Sommeil::getDateNuit, Sommeil::calculerScoreQualite,
                        (a, b) -> a, TreeMap::new));
    }

    // ═══════════════════════════════════════════════════════════
    //  ANALYSE D'UNE NUIT
    // ═══════════════════════════════════════════════════════════

    public String analyserQualiteSommeil(Sommeil sommeil) {
        if (sommeil == null) return "Aucune donnée";
        StringBuilder a = new StringBuilder();
        double duree = sommeil.getDureeSommeil();
        if      (duree < 6)              a.append("Durée insuffisante (< 6h). ");
        else if (duree >= 7 && duree <= 9) a.append("Durée optimale (7-9h). ");
        else if (duree > 9)              a.append("Durée excessive (> 9h). ");
        else                             a.append("Durée acceptable (6-7h). ");
        int inter = sommeil.getInterruptions();
        if      (inter == 0) a.append("Sommeil continu. ");
        else if (inter <= 2) a.append("Quelques interruptions. ");
        else                 a.append("Trop d'interruptions (" + inter + "). ");
        int score = sommeil.calculerScoreQualite();
        if      (score >= 80) a.append("Excellente nuit (score: " + score + "/100).");
        else if (score >= 60) a.append("Bonne nuit (score: " + score + "/100).");
        else if (score >= 40) a.append("Nuit moyenne (score: " + score + "/100).");
        else                  a.append("Nuit difficile (score: " + score + "/100).");
        return a.toString();
    }

    // ═══════════════════════════════════════════════════════════
    //  SCORE BIEN-ÊTRE & RÉSILIENCE
    // ═══════════════════════════════════════════════════════════

    public int calculerScoreBienEtre() throws SQLException {
        List<Sommeil> s = listerTous();
        if (s.isEmpty()) return 0;
        double scoreMoy    = calculerScoreMoyen();
        double dureeMoy    = calculerDureeMoyenne();
        double interMoy    = calculerMoyenneInterruptions();
        double pctOptimale = calculerPourcentageDureeOptimale();
        double cScore    = scoreMoy * 0.40;
        double cDuree    = Math.min(dureeMoy / 8.0, 1.0) * 100 * 0.30;
        double cInter    = Math.max(0, (5 - interMoy) / 5.0) * 100 * 0.15;
        double cOptimale = pctOptimale * 0.15;
        return (int) Math.round(cScore + cDuree + cInter + cOptimale);
    }

    public String libelleScore(int score) {
        if (score >= 85) return "Excellent";
        if (score >= 70) return "Bon";
        if (score >= 50) return "Moyen";
        if (score >= 30) return "Faible";
        return "Critique";
    }

    public double calculerIndexResilience() throws SQLException {
        List<Sommeil> s = listerTous();
        if (s.isEmpty()) return 0;
        long positives = s.stream()
                .filter(n -> "Excellente".equals(n.getQualite()) || "Bonne".equals(n.getQualite()))
                .count();
        return (double) positives / s.size();
    }

    public String libelleResilience(double index) {
        if (index >= 0.80) return "Très bon rythme de sommeil";
        if (index >= 0.60) return "Rythme correct";
        if (index >= 0.40) return "Rythme irrégulier";
        return "Rythme préoccupant";
    }

    // ═══════════════════════════════════════════════════════════
    //  CLASSEMENT PAR NIVEAU
    // ═══════════════════════════════════════════════════════════

    public Map<String, List<Sommeil>> classerParNiveau() throws SQLException {
        Map<String, List<Sommeil>> classes = new LinkedHashMap<>();
        classes.put("🟢 BON",    new ArrayList<>());
        classes.put("🟡 MOYEN",  new ArrayList<>());
        classes.put("🔴 MAUVAIS", new ArrayList<>());
        for (Sommeil s : listerTous()) {
            int score = s.calculerScoreQualite();
            if      (score >= 70) classes.get("🟢 BON").add(s);
            else if (score >= 40) classes.get("🟡 MOYEN").add(s);
            else                  classes.get("🔴 MAUVAIS").add(s);
        }
        return classes;
    }

    // ═══════════════════════════════════════════════════════════
    //  DÉTECTION D'ANOMALIES
    // ═══════════════════════════════════════════════════════════

    public List<Sommeil> detecterAnomalies() throws SQLException {
        List<Sommeil> s = listerTous();
        if (s.size() < 3) return Collections.emptyList();
        double moyDuree = s.stream().mapToDouble(Sommeil::getDureeSommeil).average().orElse(0);
        double sdDuree  = Math.sqrt(s.stream().mapToDouble(n -> Math.pow(n.getDureeSommeil() - moyDuree, 2)).average().orElse(0));
        double moyInter = s.stream().mapToInt(Sommeil::getInterruptions).average().orElse(0);
        double sdInter  = Math.sqrt(s.stream().mapToDouble(n -> Math.pow(n.getInterruptions() - moyInter, 2)).average().orElse(0));
        return s.stream()
                .filter(n -> n.getDureeSommeil()  < (moyDuree - 2 * sdDuree)
                        || n.getInterruptions() > (moyInter + 2 * sdInter))
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  RÉGULARITÉ DES HORAIRES
    // ═══════════════════════════════════════════════════════════

    public double calculerRegulariteHoraires() throws SQLException {
        List<Sommeil> s = listerTous().stream()
                .filter(n -> n.getHeureCoucher() != null)
                .collect(Collectors.toList());
        if (s.size() < 2) return 0;
        List<Integer> minutes = s.stream()
                .map(n -> {
                    int m = n.getHeureCoucher().getHour() * 60 + n.getHeureCoucher().getMinute();
                    return m < 12 * 60 ? m + 24 * 60 : m;
                })
                .collect(Collectors.toList());
        double moy = minutes.stream().mapToInt(Integer::intValue).average().orElse(0);
        return Math.sqrt(minutes.stream().mapToDouble(m -> Math.pow(m - moy, 2)).average().orElse(0));
    }

    public String libelleRegularite(double ecartTypeMinutes) {
        if (ecartTypeMinutes <= 15) return "Horaires très réguliers (±15 min)";
        if (ecartTypeMinutes <= 30) return "Horaires réguliers (±30 min)";
        if (ecartTypeMinutes <= 60) return "Légère irrégularité (±1h)";
        return "Horaires irréguliers (>" + String.format("%.0f", ecartTypeMinutes) + " min)";
    }

    // ═══════════════════════════════════════════════════════════
    //  DETTE DE SOMMEIL
    // ═══════════════════════════════════════════════════════════

    public double calculerDetteSommeil() throws SQLException {
        LocalDate fin   = LocalDate.now();
        LocalDate debut = fin.minusDays(7);
        List<Sommeil> s = filtrerParPeriode(debut, fin);
        if (s.isEmpty()) return 56.0;
        double totalDormi = s.stream().mapToDouble(Sommeil::getDureeSommeil).sum();
        return (8.0 * 7) - totalDormi;
    }

    public String libelleDette(double dette) {
        if      (dette <= 0)  return "Pas de dette, bravo !";
        else if (dette <= 3)  return "Dette légère ("   + String.format("%.1f", dette) + "h)";
        else if (dette <= 7)  return "Dette modérée ("  + String.format("%.1f", dette) + "h)";
        return "Dette sévère (" + String.format("%.1f", dette) + "h) — récupérez !";
    }

    // ═══════════════════════════════════════════════════════════
    //  EFFICACITÉ DU SOMMEIL
    // ═══════════════════════════════════════════════════════════

    public double calculerEfficaciteSommeil(Sommeil sommeil) {
        if (sommeil == null) return 0;
        double tempsLit = sommeil.calculerDuree();
        if (tempsLit <= 0) return 0;
        double estimeDormi = tempsLit - (sommeil.getInterruptions() * 0.25);
        return Math.max(0, Math.min(100, (estimeDormi / tempsLit) * 100));
    }

    public double calculerEfficaciteMoyenne() throws SQLException {
        List<Sommeil> s = listerTous();
        if (s.isEmpty()) return 0;
        return s.stream().mapToDouble(this::calculerEfficaciteSommeil).average().orElse(0);
    }

    public String libelleEfficacite(double pct) {
        if (pct >= 90) return "Excellente efficacité";
        if (pct >= 85) return "Bonne efficacité";
        if (pct >= 75) return "Efficacité acceptable";
        return "Efficacité faible — réduisez les interruptions";
    }

    // ═══════════════════════════════════════════════════════════
    //  TENDANCES
    // ═══════════════════════════════════════════════════════════

    public Map<String, String> identifierTendances(LocalDate debut, LocalDate fin)
            throws SQLException {
        List<Sommeil> s = filtrerParPeriode(debut, fin);
        Map<String, String> tendances = new HashMap<>();
        if (s.size() < 3) { tendances.put("message", "Pas assez de données"); return tendances; }
        tendances.put("duree",         tendance(s.stream().map(Sommeil::getDureeSommeil).collect(Collectors.toList()), true));
        tendances.put("qualite",       tendance(s.stream().map(n -> (double) n.calculerScoreQualite()).collect(Collectors.toList()), true));
        tendances.put("interruptions", tendance(s.stream().map(n -> (double) n.getInterruptions()).collect(Collectors.toList()), false));
        return tendances;
    }

    private String tendance(List<Double> valeurs, boolean croissantPositif) {
        if (valeurs.size() < 2) return "Stable";
        int augmentations = 0, diminutions = 0;
        for (int i = 1; i < valeurs.size(); i++) {
            if      (valeurs.get(i) > valeurs.get(i - 1)) augmentations++;
            else if (valeurs.get(i) < valeurs.get(i - 1)) diminutions++;
        }
        int seuil = valeurs.size() / 2;
        if      (augmentations > seuil) return croissantPositif ? "📈 Amélioration" : "📉 Dégradation";
        else if (diminutions   > seuil) return croissantPositif ? "📉 Dégradation"  : "📈 Amélioration";
        return "➡️ Stable";
    }

    // ═══════════════════════════════════════════════════════════
    //  PROFIL CHRONOBIOLOGIQUE
    // ═══════════════════════════════════════════════════════════

    public String determinerProfilChronobiologique() throws SQLException {
        List<Sommeil> s = listerTous().stream()
                .filter(n -> n.getHeureCoucher() != null).collect(Collectors.toList());
        if (s.isEmpty()) return "Profil indéterminé";
        List<Integer> minutes = s.stream()
                .map(n -> {
                    int m = n.getHeureCoucher().getHour() * 60 + n.getHeureCoucher().getMinute();
                    return m < 12 * 60 ? m + 24 * 60 : m;
                })
                .sorted().collect(Collectors.toList());
        int mediane = minutes.get(minutes.size() / 2);
        int heure   = (mediane / 60) % 24;
        if      (heure < 22) return "🌅 Chronotype matinal (couche avant 22h)";
        else if (heure < 24) return "🌙 Chronotype intermédiaire (22h–minuit)";
        return "🦉 Chronotype tardif (couche après minuit)";
    }

    // ═══════════════════════════════════════════════════════════
    //  RECOMMANDATIONS
    // ═══════════════════════════════════════════════════════════

    public List<String> genererRecommandations() throws SQLException {
        List<String> rec = new ArrayList<>();
        List<Sommeil> s = listerTous();
        if (s.isEmpty()) { rec.add("Commencez à enregistrer vos nuits de sommeil"); return rec; }
        double dureeMoy   = calculerDureeMoyenne();
        double interMoy   = calculerMoyenneInterruptions();
        double dette      = calculerDetteSommeil();
        double regularite = calculerRegulariteHoraires();
        double efficacite = calculerEfficaciteMoyenne();
        if      (dureeMoy < 7) { rec.add("Dormez au moins 7h par nuit — visez 8h"); rec.add("Couchez-vous 30 min plus tôt chaque soir"); }
        else if (dureeMoy > 9)   rec.add("Durée excessive : peut indiquer de la fatigue chronique");
        if (interMoy > 2) { rec.add("Réduisez les sources de bruit et de lumière la nuit"); rec.add("Maintenez 18–20°C dans votre chambre"); rec.add("Évitez les écrans 1h avant le coucher"); }
        if (dette > 5)           rec.add("Dette de sommeil élevée : planifiez une nuit de récupération");
        if (regularite > 45)     rec.add("Fixez des horaires de coucher et réveil fixes, même le week-end");
        if (efficacite < 80)   { rec.add("Efficacité faible : levez-vous si vous ne dormez pas après 20 min"); rec.add("Pratiquez une relaxation musculaire progressive avant de dormir"); }
        if (rec.isEmpty())       rec.add("Votre sommeil est de bonne qualité — continuez ainsi !");
        return rec;
    }

    // ═══════════════════════════════════════════════════════════
    //  INSIGHTS
    // ═══════════════════════════════════════════════════════════

    public List<String> obtenirInsights() throws SQLException {
        List<String> insights = new ArrayList<>();
        List<Sommeil> s = listerTous();
        if (s.isEmpty()) { insights.add("Aucune nuit enregistrée pour le moment."); return insights; }
        int    score      = calculerScoreBienEtre();
        double dette      = calculerDetteSommeil();
        double regularite = calculerRegulariteHoraires();
        double efficacite = calculerEfficaciteMoyenne();
        double resilience = calculerIndexResilience();
        insights.add("🏆 Score de bien-être : " + score + "/100 — " + libelleScore(score));
        insights.add("💤 Dette de sommeil (7j) : " + libelleDette(dette));
        insights.add("⏰ Régularité : " + libelleRegularite(regularite));
        insights.add("⚡ Efficacité moyenne : " + String.format("%.1f%%", efficacite) + " — " + libelleEfficacite(efficacite));
        insights.add("💪 Résilience : " + String.format("%.0f%%", resilience * 100) + " — " + libelleResilience(resilience));
        insights.add(determinerProfilChronobiologique());
        Sommeil meilleure = trouverMeilleurSommeil();
        if (meilleure != null)
            insights.add("🌟 Meilleure nuit : " + meilleure.getDateNuit() + " (score " + meilleure.calculerScoreQualite() + "/100)");
        compterParQualite().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(e -> insights.add("📊 Qualité dominante : " + e.getKey() + " (" + e.getValue() + " nuits)"));
        return insights;
    }

    // ═══════════════════════════════════════════════════════════
    //  RAPPORTS
    // ═══════════════════════════════════════════════════════════

    public String genererRapportDetaille(Sommeil sommeil) {
        if (sommeil == null) return "Aucune donnée";
        StringBuilder sb = new StringBuilder();
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n  RAPPORT DE LA NUIT\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        sb.append("Date          : ").append(sommeil.getDateNuit()).append("\n");
        sb.append("Coucher       : ").append(sommeil.getHeureCoucher()).append("\n");
        sb.append("Réveil        : ").append(sommeil.getHeureReveil()).append("\n");
        sb.append("Durée         : ").append(String.format("%.1f h", sommeil.getDureeSommeil())).append("\n");
        sb.append("Qualité       : ").append(sommeil.getQualite()).append("\n");
        sb.append("Score         : ").append(sommeil.calculerScoreQualite()).append("/100\n");
        sb.append("Interruptions : ").append(sommeil.getInterruptions()).append("\n");
        sb.append("Humeur réveil : ").append(sommeil.getHumeurReveil() != null ? sommeil.getHumeurReveil() : "—").append("\n");
        sb.append("Environnement : ").append(sommeil.getEnvironnement() != null ? sommeil.getEnvironnement() : "—").append("\n");
        sb.append("Efficacité    : ").append(String.format("%.1f%%", calculerEfficaciteSommeil(sommeil))).append("\n\n");
        sb.append("── Analyse ──────────────────────────\n").append(analyserQualiteSommeil(sommeil)).append("\n");
        return sb.toString();
    }

    public String genererRapportGlobal() throws SQLException {
        List<Sommeil> s = listerTous();
        StringBuilder sb = new StringBuilder();
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n  RAPPORT GLOBAL DU SOMMEIL\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        if (s.isEmpty()) { sb.append("Aucune nuit enregistrée."); return sb.toString(); }
        sb.append("Total nuits     : ").append(s.size()).append("\n");
        sb.append("Durée moyenne   : ").append(String.format("%.1f h",   calculerDureeMoyenne())).append("\n");
        sb.append("Score moyen     : ").append(String.format("%.1f/100", calculerScoreMoyen())).append("\n");
        sb.append("Interruptions   : ").append(String.format("%.1f/nuit", calculerMoyenneInterruptions())).append("\n");
        sb.append("Bien-être       : ").append(calculerScoreBienEtre()).append("/100\n");
        sb.append("Résilience      : ").append(String.format("%.0f%%", calculerIndexResilience() * 100)).append("\n");
        sb.append("Dette (7j)      : ").append(libelleDette(calculerDetteSommeil())).append("\n");
        sb.append("Efficacité moy. : ").append(String.format("%.1f%%", calculerEfficaciteMoyenne())).append("\n");
        sb.append("Régularité      : ").append(libelleRegularite(calculerRegulariteHoraires())).append("\n");
        sb.append("Profil          : ").append(determinerProfilChronobiologique()).append("\n\n");
        sb.append("── Répartition qualité ──────────────\n");
        compterParQualite().forEach((q, n) -> sb.append("  ").append(q).append(" : ").append(n).append(" nuits\n"));
        sb.append("\n── Insights ─────────────────────────\n");
        obtenirInsights().forEach(i -> sb.append("  ").append(i).append("\n"));
        sb.append("\n── Recommandations ──────────────────\n");
        genererRecommandations().forEach(r -> sb.append("  • ").append(r).append("\n"));
        return sb.toString();
    }

    // ─── Utilitaires privés ──────────────────────────────────────────────────────

    private double calculerEcartTypeDuree(List<Sommeil> sommeils) {
        if (sommeils.isEmpty()) return 0;
        double moyenne = sommeils.stream().mapToDouble(Sommeil::getDureeSommeil).average().orElse(0);
        return Math.sqrt(sommeils.stream()
                .mapToDouble(s -> Math.pow(s.getDureeSommeil() - moyenne, 2)).average().orElse(0));
    }
}
