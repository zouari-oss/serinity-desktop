package com.serinity.sleepcontrol.ia;

import com.serinity.sleepcontrol.model.Reve;

import java.util.*;

public class ReveAnalyseIA {

    // ═══════════════════════════════════════════════════════════
    //  DICTIONNAIRES PSYCHOLOGIQUES
    // ═══════════════════════════════════════════════════════════

    private static final Map<String, String> SYMBOLES_PSYCHO = new LinkedHashMap<>() {{
        put("eau",       "Émotions profondes, inconscient, transformation intérieure");
        put("feu",       "Passion, colère refoulée, transformation, danger perçu");
        put("chute",     "Perte de contrôle, anxiété, insécurité dans la vie réelle");
        put("vol",       "Désir de liberté, ambitions élevées, fuite d'une situation");
        put("mort",      "Fin d'un cycle, changement majeur, peur de la perte");
        put("poursuite", "Évitement d'un problème, stress, pression externe");
        put("maison",    "Soi intérieur, famille, sentiment de sécurité ou d'insécurité");
        put("animal",    "Instincts primaires, émotions brutes, aspects de la personnalité");
        put("enfant",    "Innocence, vulnérabilité, aspects non développés de soi");
        put("route",     "Parcours de vie, décisions à prendre, direction future");
        put("dent",      "Anxiété sociale, peur du jugement, perte de pouvoir");
        put("argent",    "Valeur personnelle, sécurité matérielle, estime de soi");
        put("école",     "Pression de performance, jugement, apprentissage non terminé");
        put("mer",       "Vastitude de l'inconscient, liberté, profondeur émotionnelle");
        put("forêt",     "Aspects inconnus de soi, exploration intérieure, mystère");
        put("lumière",   "Espoir, clarté mentale, révélation, éveil spirituel");
        put("obscurité", "Peur, incertitude, aspects refoulés, dépression latente");
        put("serpent",   "Trahison, guérison, transformation, sexualité");
        put("araignée",  "Piège, manipulation, créativité, peur de la dépendance");
        put("vol",       "Liberté, ambition, perspective élevée sur les problèmes");
    }};

    private static final Map<String, Integer> EMOTIONS_SCORE = new HashMap<>() {{
        put("joie",        +15);
        put("amour",       +15);
        put("sérénité",    +12);
        put("bonheur",     +12);
        put("excitation",  +8);
        put("curiosité",   +5);
        put("neutre",       0);
        put("tristesse",   -8);
        put("solitude",    -8);
        put("confusion",   -5);
        put("colère",      -10);
        put("peur",        -12);
        put("anxiété",     -15);
        put("terreur",     -18);
        put("honte",       -10);
        put("culpabilité", -12);
    }};

    private static final Map<String, String[]> PROFILS_PSY = new LinkedHashMap<>() {{
        put("ANXIEUX",      new String[]{"peur", "anxiété", "poursuite", "chute", "terreur", "dent"});
        put("CRÉATIF",      new String[]{"vol", "lumière", "couleur", "transformation", "art"});
        put("NOSTALGIQUE",  new String[]{"enfant", "école", "maison", "passé", "famille"});
        put("CONFLICTUEL",  new String[]{"colère", "combat", "poursuite", "dispute", "guerre"});
        put("SPIRITUEL",    new String[]{"lumière", "mort", "transformation", "eau", "ciel"});
        put("INSÉCURISÉ",   new String[]{"chute", "maison", "dent", "argent", "perte"});
        put("LIBÉRATEUR",   new String[]{"vol", "mer", "liberté", "route", "fuite"});
    }};

    // ═══════════════════════════════════════════════════════════
    //  ANALYSE PRINCIPALE
    // ═══════════════════════════════════════════════════════════

    public AnalyseResult analyser(Reve reve) {
        AnalyseResult result = new AnalyseResult();

        result.setScorePsychologique(calculerScorePsychologique(reve));
        result.setProfilDominant(determinerProfil(reve));
        result.setSymbolesDetectes(analyserSymboles(reve));
        result.setImpactEmotionnel(evaluerImpactEmotionnel(reve));
        result.setConclusion(genererConclusion(reve, result));
        result.setRecommandations(genererRecommandations(reve, result));
        result.setNiveauAlerte(calculerNiveauAlerte(reve, result));

        return result;
    }

    public AnalyseResult analyserTous(List<Reve> reves) {
        if (reves == null || reves.isEmpty()) {
            AnalyseResult vide = new AnalyseResult();
            vide.setConclusion("Aucun rêve enregistré pour l'analyse.");
            return vide;
        }

        AnalyseResult result = new AnalyseResult();

        // Score moyen
        double scoreMoy = reves.stream()
                .mapToInt(r -> calculerScorePsychologique(r))
                .average().orElse(50);
        result.setScorePsychologique((int) scoreMoy);

        // Profil dominant global
        Map<String, Integer> profilCount = new HashMap<>();
        for (Reve r : reves) {
            String profil = determinerProfil(r);
            profilCount.merge(profil, 1, Integer::sum);
        }
        result.setProfilDominant(profilCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("ÉQUILIBRÉ"));

        // Symboles les plus fréquents
        Map<String, Integer> symbolesFreq = new HashMap<>();
        for (Reve r : reves) {
            analyserSymboles(r).forEach((sym, desc) ->
                    symbolesFreq.merge(sym, 1, Integer::sum));
        }
        // Garder top 5
        Map<String, String> top5 = new LinkedHashMap<>();
        symbolesFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> top5.put(e.getKey() + " (×" + e.getValue() + ")",
                        SYMBOLES_PSYCHO.getOrDefault(e.getKey(), "Symbole récurrent")));
        result.setSymbolesDetectes(top5);

        result.setImpactEmotionnel(evaluerImpactGlobal(reves));
        result.setConclusion(genererConclusionGlobale(reves, result));
        result.setRecommandations(genererRecommandationsGlobales(reves, result));
        result.setNiveauAlerte(calculerAlerteGlobale(reves));

        return result;
    }

    // ═══════════════════════════════════════════════════════════
    //  SCORING PSYCHOLOGIQUE
    // ═══════════════════════════════════════════════════════════

    private int calculerScorePsychologique(Reve reve) {
        int score = 50; // base neutre

        // Impact de l'intensité
        if (reve.getIntensite() >= 8)      score -= 10;
        else if (reve.getIntensite() <= 3) score += 5;

        // Impact de l'anxiété
        int anxiete = reve.calculerNiveauAnxiete();
        score -= anxiete * 3;

        // Impact du type
        switch (reve.getTypeReve().toLowerCase()) {
            case "cauchemar"  -> score -= 20;
            case "lucide"     -> score += 15;
            case "recurrent"  -> score -= 10;
            default           -> score += 5;
        }

        // Impact des émotions
        if (reve.getEmotions() != null) {
            String emo = reve.getEmotions().toLowerCase();
            for (Map.Entry<String, Integer> e : EMOTIONS_SCORE.entrySet()) {
                if (emo.contains(e.getKey())) score += e.getValue();
            }
        }

        // Rêve en couleur = signe positif
        if (reve.isCouleur()) score += 5;

        return Math.max(0, Math.min(100, score));
    }

    // ═══════════════════════════════════════════════════════════
    //  PROFIL PSYCHOLOGIQUE
    // ═══════════════════════════════════════════════════════════

    private String determinerProfil(Reve reve) {
        String texte = buildTexteReve(reve).toLowerCase();
        Map<String, Integer> scores = new HashMap<>();

        for (Map.Entry<String, String[]> profil : PROFILS_PSY.entrySet()) {
            int s = 0;
            for (String mot : profil.getValue()) {
                if (texte.contains(mot)) s++;
            }
            if (s > 0) scores.put(profil.getKey(), s);
        }

        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("ÉQUILIBRÉ");
    }

    // ═══════════════════════════════════════════════════════════
    //  ANALYSE DES SYMBOLES
    // ═══════════════════════════════════════════════════════════

    private Map<String, String> analyserSymboles(Reve reve) {
        String texte = buildTexteReve(reve).toLowerCase();
        Map<String, String> trouves = new LinkedHashMap<>();

        for (Map.Entry<String, String> symbole : SYMBOLES_PSYCHO.entrySet()) {
            if (texte.contains(symbole.getKey())) {
                trouves.put(symbole.getKey(), symbole.getValue());
            }
        }
        return trouves;
    }

    // ═══════════════════════════════════════════════════════════
    //  IMPACT ÉMOTIONNEL
    // ═══════════════════════════════════════════════════════════

    private String evaluerImpactEmotionnel(Reve reve) {
        int score = calculerScorePsychologique(reve);
        int anxiete = reve.calculerNiveauAnxiete();

        if (score >= 75)           return "🟢 Impact positif — Ce rêve reflète un état mental serein";
        if (score >= 55)           return "🔵 Impact neutre — Traitement ordinaire de la journée";
        if (score >= 35)           return "🟡 Impact modéré — Légère tension psychologique détectée";
        if (anxiete >= 7)          return "🔴 Impact négatif sévère — Anxiété importante à surveiller";
        return                            "🟠 Impact négatif — Stress ou conflit intérieur présent";
    }

    private String evaluerImpactGlobal(List<Reve> reves) {
        double moy = reves.stream()
                .mapToInt(this::calculerScorePsychologique)
                .average().orElse(50);
        long cauchemars = reves.stream()
                .filter(r -> "cauchemar".equalsIgnoreCase(r.getTypeReve())).count();
        double pctC = (cauchemars * 100.0) / reves.size();

        if (moy >= 70 && pctC < 20) return "🟢 État psychologique globalement positif";
        if (moy >= 55)              return "🔵 État psychologique stable avec quelques tensions";
        if (pctC >= 40)             return "🔴 Taux de cauchemars élevé — stress chronique possible";
        return                             "🟠 État psychologique à surveiller — tensions récurrentes";
    }

    // ═══════════════════════════════════════════════════════════
    //  CONCLUSIONS
    // ═══════════════════════════════════════════════════════════

    private String genererConclusion(Reve reve, AnalyseResult result) {
        StringBuilder sb = new StringBuilder();
        int score = result.getScorePsychologique();
        String profil = result.getProfilDominant();

        sb.append("Ce rêve présente un profil psychologique de type ")
                .append(profil).append(". ");

        switch (profil) {
            case "ANXIEUX" -> sb.append(
                    "Les thèmes récurrents d'anxiété suggèrent un stress quotidien non résolu. " +
                            "Votre cerveau tente de traiter des situations perçues comme menaçantes.");
            case "CRÉATIF" -> sb.append(
                    "Ce rêve révèle une activité mentale riche et créative. " +
                            "Votre esprit explore de nouvelles possibilités et solutions.");
            case "NOSTALGIQUE" -> sb.append(
                    "Des liens émotionnels forts avec le passé sont présents. " +
                            "Votre inconscient revisit des expériences formatrices.");
            case "CONFLICTUEL" -> sb.append(
                    "Des tensions internes ou externes non résolues se manifestent. " +
                            "Un conflit psychologique actif est en cours de traitement.");
            case "SPIRITUEL" -> sb.append(
                    "Ce rêve indique une recherche de sens et de transformation profonde. " +
                            "Une phase de transition importante dans votre vie est probable.");
            case "INSÉCURISÉ" -> sb.append(
                    "Des besoins de sécurité et de stabilité dominent votre inconscient. " +
                            "Des incertitudes dans votre vie éveillée alimentent ces thèmes.");
            case "LIBÉRATEUR" -> sb.append(
                    "Votre inconscient exprime un fort désir de liberté et d'évasion. " +
                            "Une situation contraignante dans votre vie réelle peut en être la cause.");
            default -> sb.append(
                    "Votre état psychologique apparaît globalement équilibré. " +
                            "Ce rêve reflète un traitement normal des événements quotidiens.");
        }

        sb.append("\n\nScore psychologique : ").append(score).append("/100 — ");
        if      (score >= 75) sb.append("État mental positif.");
        else if (score >= 50) sb.append("État mental stable.");
        else if (score >= 30) sb.append("Légère fragilité émotionnelle.");
        else                  sb.append("Attention requise sur votre bien-être mental.");

        return sb.toString();
    }

    private String genererConclusionGlobale(List<Reve> reves, AnalyseResult result) {
        StringBuilder sb = new StringBuilder();
        long lucides    = reves.stream().filter(r -> "lucide".equalsIgnoreCase(r.getTypeReve())).count();
        long cauchemars = reves.stream().filter(r -> "cauchemar".equalsIgnoreCase(r.getTypeReve())).count();
        long recurrents = reves.stream().filter(Reve::isRecurrent).count();
        double anxMoy   = reves.stream().mapToInt(r -> r.calculerNiveauAnxiete()).average().orElse(0);

        sb.append("Analyse de ").append(reves.size()).append(" rêves enregistrés.\n\n");
        sb.append("Profil dominant : ").append(result.getProfilDominant()).append("\n");
        sb.append("• ").append(cauchemars).append(" cauchemar(s) détecté(s)");
        if (cauchemars > reves.size() * 0.3) sb.append(" ⚠️ taux élevé");
        sb.append("\n• ").append(lucides).append(" rêve(s) lucide(s)");
        if (lucides > 0) sb.append(" ✅ signe de conscience onirique développée");
        sb.append("\n• ").append(recurrents).append(" rêve(s) récurrent(s)");
        if (recurrents > 2) sb.append(" — thème non résolu à explorer");
        sb.append("\n• Anxiété moyenne : ").append(String.format("%.1f", anxMoy)).append("/10");
        if (anxMoy >= 6) sb.append(" 🔴 niveau préoccupant");
        else if (anxMoy >= 4) sb.append(" 🟡 niveau modéré");
        else sb.append(" 🟢 niveau sain");

        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════
    //  RECOMMANDATIONS
    // ═══════════════════════════════════════════════════════════

    private List<String> genererRecommandations(Reve reve, AnalyseResult result) {
        List<String> rec = new ArrayList<>();
        String profil = result.getProfilDominant();
        int anxiete   = reve.calculerNiveauAnxiete();

        switch (profil) {
            case "ANXIEUX", "INSÉCURISÉ" -> {
                rec.add("🧘 Pratiquez 10 min de méditation avant de dormir");
                rec.add("📔 Tenez un journal de gratitude pour ancrer le positif");
                rec.add("🌬️ Appliquez la respiration 4-7-8 en cas de stress nocturne");
            }
            case "CONFLICTUEL" -> {
                rec.add("💬 Identifiez et exprimez le conflit non résolu dans votre vie éveillée");
                rec.add("🧠 Envisagez une thérapie cognitivo-comportementale (TCC)");
                rec.add("✍️ Écrivez vos pensées conflictuelles avant de dormir");
            }
            case "CRÉATIF", "SPIRITUEL" -> {
                rec.add("🎨 Exploitez cette énergie créative dans une activité artistique");
                rec.add("📖 Tenez un journal de rêves pour capturer vos insights");
                rec.add("🌙 Pratiquez l'incubation de rêves pour orienter votre créativité");
            }
            case "NOSTALGIQUE" -> {
                rec.add("💭 Accordez-vous du temps pour intégrer les souvenirs importants");
                rec.add("🤝 Reconnectez-vous avec des proches du passé si approprié");
                rec.add("📸 L'album photo ou écriture peut aider à traiter la nostalgie");
            }
            case "LIBÉRATEUR" -> {
                rec.add("🗺️ Identifiez ce qui vous contraint dans votre vie réelle");
                rec.add("🎯 Fixez un objectif concret vers plus de liberté personnelle");
                rec.add("🏃 L'exercice physique régulier aide à libérer les tensions");
            }
            default -> rec.add("✅ Continuez vos bonnes habitudes de sommeil");
        }

        if (anxiete >= 7) {
            rec.add("⚠️ Niveau d'anxiété élevé : consultez un professionnel de santé mentale");
        }
        if ("cauchemar".equalsIgnoreCase(reve.getTypeReve())) {
            rec.add("🌙 Technique de répétition par imagerie (IRT) recommandée pour les cauchemars");
        }
        if (reve.isRecurrent()) {
            rec.add("🔁 Ce rêve récurrent mérite une exploration en thérapie — message non traité");
        }

        return rec;
    }

    private List<String> genererRecommandationsGlobales(List<Reve> reves, AnalyseResult result) {
        List<String> rec = new ArrayList<>();
        double anxMoy   = reves.stream().mapToInt(r -> r.calculerNiveauAnxiete()).average().orElse(0);
        long cauchemars = reves.stream().filter(r -> "cauchemar".equalsIgnoreCase(r.getTypeReve())).count();
        long recurrents = reves.stream().filter(Reve::isRecurrent).count();

        if (anxMoy >= 6)
            rec.add("⚠️ Anxiété onirique chronique — consultez un spécialiste du sommeil");
        if (cauchemars >= reves.size() * 0.4)
            rec.add("😱 Trop de cauchemars : réduisez stress, caféine et écrans le soir");
        if (recurrents >= 3)
            rec.add("🔁 Plusieurs rêves récurrents — explorez le thème sous-jacent non résolu");

        rec.add("📔 Tenez un journal de rêves dès le réveil pour mieux les analyser");
        rec.add("😴 Maintenez 7-9h de sommeil régulier pour une meilleure santé mentale");
        rec.add("🧘 La méditation pleine conscience améliore la qualité des rêves");

        return rec;
    }

    // ═══════════════════════════════════════════════════════════
    //  NIVEAU D'ALERTE
    // ═══════════════════════════════════════════════════════════

    private String calculerNiveauAlerte(Reve reve, AnalyseResult result) {
        int score   = result.getScorePsychologique();
        int anxiete = reve.calculerNiveauAnxiete();
        if (score < 25 || anxiete >= 8) return "CRITIQUE";
        if (score < 40 || anxiete >= 6) return "ÉLEVÉ";
        if (score < 55)                 return "MODÉRÉ";
        if (score < 75)                 return "FAIBLE";
        return "AUCUN";
    }

    private String calculerAlerteGlobale(List<Reve> reves) {
        double scoreMoy = reves.stream()
                .mapToInt(this::calculerScorePsychologique)
                .average().orElse(50);
        double anxMoy = reves.stream()
                .mapToInt(r -> r.calculerNiveauAnxiete())
                .average().orElse(0);
        if (scoreMoy < 25 || anxMoy >= 8) return "CRITIQUE";
        if (scoreMoy < 40 || anxMoy >= 6) return "ÉLEVÉ";
        if (scoreMoy < 55)                return "MODÉRÉ";
        if (scoreMoy < 75)                return "FAIBLE";
        return "AUCUN";
    }

    // ═══════════════════════════════════════════════════════════
    //  UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    private String buildTexteReve(Reve reve) {
        StringBuilder sb = new StringBuilder();
        if (reve.getTitre()      != null) sb.append(reve.getTitre()).append(" ");
        if (reve.getDescription()!= null) sb.append(reve.getDescription()).append(" ");
        if (reve.getEmotions()   != null) sb.append(reve.getEmotions()).append(" ");
        if (reve.getSymboles()   != null) sb.append(reve.getSymboles()).append(" ");
        if (reve.getTypeReve()   != null) sb.append(reve.getTypeReve()).append(" ");
        return sb.toString();
    }
}
