package com.serinity.sleepcontrol.service;

import com.serinity.sleepcontrol.dao.ReveDao;
import com.serinity.sleepcontrol.dao.impl.ReveDaoJdbc;
import com.serinity.sleepcontrol.model.Reve;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ReveService {

    private final ReveDao reveDao;

    public ReveService() {
        this.reveDao = new ReveDaoJdbc();
    }

    public ReveService(ReveDao reveDao) {
        this.reveDao = reveDao;
    }

    // ═══════════════════════════════════════════════════════════
    //  CRUD
    // ═══════════════════════════════════════════════════════════

    public void creer(Reve reve) throws SQLException {
        if (reve == null)
            throw new IllegalArgumentException("Le rêve ne peut pas être null");
        if (!reve.estValide())
            throw new IllegalArgumentException("Les données du rêve sont invalides");
        if (reve.getSommeilId() <= 0)
            throw new IllegalArgumentException("Le rêve doit être associé à un sommeil");
        reveDao.ajouter(reve);
    }

    public List<Reve> listerTous() throws SQLException {
        return reveDao.listerTous();
    }

    public Reve trouverParId(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("L'ID doit être positif");
        return reveDao.trouverParId(id);
    }

    public List<Reve> trouverParSommeilId(int sommeilId) throws SQLException {
        if (sommeilId <= 0)
            throw new IllegalArgumentException("L'ID du sommeil doit être positif");
        return reveDao.trouverParSommeilId(sommeilId);
    }

    public void modifier(Reve reve) throws SQLException {
        if (reve == null || reve.getId() <= 0)
            throw new IllegalArgumentException("Rêve invalide pour modification");
        if (!reve.estValide())
            throw new IllegalArgumentException("Les données du rêve sont invalides");
        reveDao.modifier(reve);
    }

    public void supprimer(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("L'ID doit être positif");
        reveDao.supprimer(id);
    }

    public boolean existeParTitre(String titre) throws SQLException {
        if (titre == null || titre.trim().isEmpty()) return false;
        return listerTous().stream()
                .anyMatch(r -> titre.equalsIgnoreCase(r.getTitre()));
    }

    // ═══════════════════════════════════════════════════════════
    //  RECHERCHE
    // ═══════════════════════════════════════════════════════════

    public List<Reve> rechercherDynamique(String critere) throws SQLException {
        if (critere == null || critere.trim().isEmpty()) return listerTous();
        return reveDao.rechercher(critere.trim());
    }

    public List<Reve> rechercherAvancee(String type, String humeur,
                                        Integer intensiteMin, Integer intensiteMax,
                                        Boolean recurrent) throws SQLException {
        List<Reve> res = listerTous();

        if (type != null && !type.isEmpty())
            res = res.stream()
                    .filter(r -> r.getTypeReve().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        if (humeur != null && !humeur.isEmpty())
            res = res.stream()
                    .filter(r -> humeur.equalsIgnoreCase(r.getHumeur()))
                    .collect(Collectors.toList());
        if (intensiteMin != null)
            res = res.stream()
                    .filter(r -> r.getIntensite() >= intensiteMin)
                    .collect(Collectors.toList());
        if (intensiteMax != null)
            res = res.stream()
                    .filter(r -> r.getIntensite() <= intensiteMax)
                    .collect(Collectors.toList());
        if (recurrent != null)
            res = res.stream()
                    .filter(r -> r.isRecurrent() == recurrent)
                    .collect(Collectors.toList());
        return res;
    }

    // ═══════════════════════════════════════════════════════════
    //  TRIS
    // ═══════════════════════════════════════════════════════════

    public List<Reve> trierParIntensite(boolean croissant) throws SQLException {
        Comparator<Reve> c = Comparator.comparingInt(Reve::getIntensite);
        return listerTous().stream()
                .sorted(croissant ? c : c.reversed())
                .collect(Collectors.toList());
    }

    public List<Reve> trierParTitre(boolean croissant) throws SQLException {
        Comparator<Reve> c = Comparator.comparing(Reve::getTitre);
        return listerTous().stream()
                .sorted(croissant ? c : c.reversed())
                .collect(Collectors.toList());
    }

    public List<Reve> trierParType(boolean croissant) throws SQLException {
        Map<String, Integer> ordreType = new HashMap<>();
        ordreType.put("Lucide", 4);
        ordreType.put("Normal", 3);
        ordreType.put("Récurrent", 2);
        ordreType.put("Cauchemar", 1);

        Comparator<Reve> c = Comparator.comparingInt(
                r -> ordreType.getOrDefault(r.getTypeReve(), 0));
        return listerTous().stream()
                .sorted(croissant ? c : c.reversed())
                .collect(Collectors.toList());
    }

    public List<Reve> trierParAnxiete(boolean croissant) throws SQLException {
        Comparator<Reve> c = Comparator.comparingInt(Reve::calculerNiveauAnxiete);
        return listerTous().stream()
                .sorted(croissant ? c : c.reversed())
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  FILTRES
    // ═══════════════════════════════════════════════════════════

    public List<Reve> filtrerParType(String type) throws SQLException {
        if (type == null || type.trim().isEmpty()) return listerTous();
        return reveDao.filtrerParType(type);
    }

    public List<Reve> filtrerParIntensite(int min, int max) throws SQLException {
        if (min < 1 || max > 10 || min > max)
            throw new IllegalArgumentException("Plage d'intensité invalide (1-10)");
        return listerTous().stream()
                .filter(r -> r.getIntensite() >= min && r.getIntensite() <= max)
                .collect(Collectors.toList());
    }

    public List<Reve> filtrerRecurrents() throws SQLException {
        return listerTous().stream().filter(Reve::isRecurrent).collect(Collectors.toList());
    }

    public List<Reve> filtrerCauchemars() throws SQLException {
        return listerTous().stream().filter(Reve::estCauchemar).collect(Collectors.toList());
    }

    public List<Reve> filtrerLucides() throws SQLException {
        return listerTous().stream().filter(Reve::estLucide).collect(Collectors.toList());
    }

    public List<Reve> filtrerParHumeur(String humeur) throws SQLException {
        if (humeur == null || humeur.trim().isEmpty()) return listerTous();
        return listerTous().stream()
                .filter(r -> humeur.equalsIgnoreCase(r.getHumeur()))
                .collect(Collectors.toList());
    }

    public List<Reve> filtrerEnCouleur(boolean couleur) throws SQLException {
        return listerTous().stream()
                .filter(r -> r.isCouleur() == couleur)
                .collect(Collectors.toList());
    }

    public List<Reve> filtrerParNiveauAnxiete(int niveauMin) throws SQLException {
        return listerTous().stream()
                .filter(r -> r.calculerNiveauAnxiete() >= niveauMin)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  STATISTIQUES DE BASE
    // ═══════════════════════════════════════════════════════════

    public List<Object[]> obtenirStatistiquesParType() throws SQLException {
        return reveDao.statistiquesParType();
    }

    public Map<String, Long> compterParType() throws SQLException {
        return listerTous().stream()
                .collect(Collectors.groupingBy(Reve::getTypeReve, Collectors.counting()));
    }

    public Map<String, Long> compterParHumeur() throws SQLException {
        return listerTous().stream()
                .filter(r -> r.getHumeur() != null)
                .collect(Collectors.groupingBy(Reve::getHumeur, Collectors.counting()));
    }

    private double calculerPourcentageEnCouleur() throws SQLException {
        List<Reve> r = listerTous();
        if (r.isEmpty()) return 0;
        return (r.stream().filter(Reve::isCouleur).count() * 100.0) / r.size();
    }

    public Map<String, Long> emotionsFrequentes() throws SQLException {
        return listerTous().stream()
                .flatMap(r -> r.getEmotionsList().stream())
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));
    }

    public Map<String, Long> symbolesFrequents() throws SQLException {
        return listerTous().stream()
                .flatMap(r -> r.getSymbolesList().stream())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));
    }

    public Reve trouverPlusIntense() throws SQLException {
        return listerTous().stream()
                .max(Comparator.comparingInt(Reve::getIntensite)).orElse(null);
    }

    public Reve trouverPlusAnxiogene() throws SQLException {
        return listerTous().stream()
                .max(Comparator.comparingInt(Reve::calculerNiveauAnxiete)).orElse(null);
    }

    // ═══════════════════════════════════════════════════════════
    //  ANALYSE & RECOMMANDATIONS
    // ═══════════════════════════════════════════════════════════

    public String analyserReve(Reve reve) {
        if (reve == null) return "Aucune donnée";
        StringBuilder a = new StringBuilder();

        if      (reve.estCauchemar()) a.append("Cauchemar détecté. ");
        else if (reve.estLucide())    a.append("Rêve lucide (contrôle conscient). ");
        else                          a.append("Rêve normal. ");

        int intensite = reve.getIntensite();
        if      (intensite >= 8) a.append("Très intense. ");
        else if (intensite >= 6) a.append("Intensité modérée. ");
        else                     a.append("Faible intensité. ");

        int anxiete = reve.calculerNiveauAnxiete();
        if      (anxiete >= 7) a.append("Niveau d'anxiété élevé. ");
        else if (anxiete >= 4) a.append("Anxiété modérée. ");
        else                   a.append("Peu d'anxiété. ");

        if (reve.isRecurrent())
            a.append("Rêve récurrent (peut avoir une signification particulière). ");

        return a.toString();
    }

    public List<String> genererRecommandations() throws SQLException {
        List<String> rec = new ArrayList<>();
        List<Reve> reves = listerTous();

        if (reves.isEmpty()) {
            rec.add("Commencez à enregistrer vos rêves pour obtenir des recommandations");
            return rec;
        }

        // Les indicateurs dashboard ont été retirés, donc pas d'utilisation ici
        // (aucune autre modification)

        if (rec.isEmpty())
            rec.add("Vos rêves semblent équilibrés, continuez votre suivi!");

        return rec;
    }

    // ═══════════════════════════════════════════════════════════
    //  CLASSIFICATION PAR RISQUE
    // ═══════════════════════════════════════════════════════════

    /**
     * Classe les rêves en 3 niveaux selon le niveau d'anxiété calculé.
     * @return Map avec clés "🔴 HIGH", "🟡 MEDIUM", "🟢 LOW"
     */
    public Map<String, List<Reve>> classerParNiveauRisque() throws SQLException {
        Map<String, List<Reve>> classes = new LinkedHashMap<>();
        classes.put("🔴 HIGH",   new ArrayList<>());
        classes.put("🟡 MEDIUM", new ArrayList<>());
        classes.put("🟢 LOW",    new ArrayList<>());

        for (Reve r : listerTous()) {
            int anxiete = r.calculerNiveauAnxiete();
            if      (anxiete >= 7) classes.get("🔴 HIGH").add(r);
            else if (anxiete >= 4) classes.get("🟡 MEDIUM").add(r);
            else                   classes.get("🟢 LOW").add(r);
        }
        return classes;
    }

    // ═══════════════════════════════════════════════════════════
    //  DÉTECTION D'ANOMALIES
    // ═══════════════════════════════════════════════════════════

    /**
     * Rêves statistiquement inhabituels : intensité > moyenne + 2×écart-type.
     */
    public List<Reve> detecterAnomalies() throws SQLException {
        List<Reve> reves = listerTous();
        if (reves.size() < 3) return Collections.emptyList();

        double moyenne  = reves.stream().mapToInt(Reve::getIntensite).average().orElse(0);
        double variance = reves.stream()
                .mapToDouble(r -> Math.pow(r.getIntensite() - moyenne, 2))
                .average().orElse(0);
        double ecartType = Math.sqrt(variance);
        double seuil    = moyenne + 2 * ecartType;

        return reves.stream()
                .filter(r -> r.getIntensite() > seuil)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  THÈMES RÉCURRENTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Détecte les symboles/émotions qui apparaissent au moins {@code seuil} fois.
     */
    public Map<String, Long> detecterThemesRecurrents(int seuil) throws SQLException {
        Map<String, Long> tous = new LinkedHashMap<>();

        emotionsFrequentes().forEach((k, v) -> { if (v >= seuil) tous.put(k, v); });
        symbolesFrequents().forEach((k, v)   -> { if (v >= seuil) tous.put(k, v); });

        return tous.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));
    }

    // ═══════════════════════════════════════════════════════════
    //  RÊVES SIMILAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Retourne les rêves proches du rêve donné (même type OU même humeur,
     * et intensité dans un rayon de ±2).
     */
    public List<Reve> trouverRevesSimilaires(Reve reference) throws SQLException {
        if (reference == null) return Collections.emptyList();
        return listerTous().stream()
                .filter(r -> r.getId() != reference.getId())
                .filter(r ->
                        (r.getTypeReve() != null
                                && r.getTypeReve().equalsIgnoreCase(reference.getTypeReve()))
                                || (r.getHumeur() != null
                                && r.getHumeur().equalsIgnoreCase(reference.getHumeur()))
                )
                .filter(r -> Math.abs(r.getIntensite() - reference.getIntensite()) <= 2)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  INTENSITÉ MOYENNE PAR TYPE  (nouveau)
    // ═══════════════════════════════════════════════════════════

    public Map<String, Double> intensiteMoyenneParType() throws SQLException {
        return listerTous().stream()
                .collect(Collectors.groupingBy(
                        Reve::getTypeReve,
                        Collectors.averagingInt(Reve::getIntensite)
                ));
    }

    // ═══════════════════════════════════════════════════════════
    //  INSIGHTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Génère une liste de phrases clés décrivant le profil onirique.
     */
    public List<String> obtenirInsights() throws SQLException {
        List<String> insights = new ArrayList<>();
        List<Reve> reves = listerTous();
        if (reves.isEmpty()) {
            insights.add("Aucun rêve enregistré pour le moment.");
            return insights;
        }

        // Les 2 blocs ci-dessous utilisaient les métriques du dashboard retirées,
        // donc ils sont supprimés pour éviter une erreur de compilation :
        // - Score de bien-être onirique
        // - Résilience

        long nbLucides = reves.stream().filter(Reve::estLucide).count();
        if (nbLucides > 0)
            insights.add(" " + nbLucides + " rêve(s) lucide(s) — capacité de contrôle développée");

        Map<String, Long> themes = detecterThemesRecurrents(2);
        if (!themes.isEmpty()) {
            String top = themes.keySet().iterator().next();
            insights.add(" Thème le plus récurrent : \"" + top + "\"");
        }

        Map<String, Double> parType = intensiteMoyenneParType();
        parType.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(e -> insights.add(
                        "⚡ Type le plus intense : " + e.getKey()
                                + " (" + String.format("%.1f", e.getValue()) + "/10)"
                ));

        return insights;
    }

    // ═══════════════════════════════════════════════════════════
    //  RAPPORT DÉTAILLÉ D'UN RÊVE
    // ═══════════════════════════════════════════════════════════

    public String genererRapportDetaille(Reve reve) {
        if (reve == null) return "Aucune donnée";

        StringBuilder sb = new StringBuilder();
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("  RAPPORT DU RÊVE\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        sb.append("Titre      : ").append(reve.getTitre()).append("\n");
        sb.append("Type       : ").append(reve.getTypeReve()).append("\n");
        sb.append("Humeur     : ").append(reve.getHumeur() != null ? reve.getHumeur() : "—").append("\n");
        sb.append("Intensité  : ").append(reve.getIntensite()).append("/10\n");
        sb.append("Anxiété    : ").append(reve.calculerNiveauAnxiete()).append("/10\n");
        sb.append("En couleur : ").append(reve.isCouleur()   ? "Oui" : "Non").append("\n");
        sb.append("Récurrent  : ").append(reve.isRecurrent() ? "Oui" : "Non").append("\n\n");

        sb.append("Description :\n").append(
                reve.getDescription() != null ? reve.getDescription() : "—").append("\n\n");

        if (reve.getEmotions() != null && !reve.getEmotions().isBlank())
            sb.append("Émotions  : ").append(reve.getEmotions()).append("\n");
        if (reve.getSymboles() != null && !reve.getSymboles().isBlank())
            sb.append("Symboles  : ").append(reve.getSymboles()).append("\n");

        sb.append("\n── Analyse ──────────────────────────\n");
        sb.append(analyserReve(reve)).append("\n");

        return sb.toString();
    }

}