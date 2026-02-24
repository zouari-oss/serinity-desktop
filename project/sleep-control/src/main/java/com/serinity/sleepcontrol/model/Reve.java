package com.serinity.sleepcontrol.model;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Reve {

    private int id;
    private String titre;
    private String description;
    private String humeur;
    private String typeReve;

    private int intensite;
    private boolean couleur;
    private String emotions;
    private String symboles;
    private boolean recurrent;

    private Sommeil sommeil;
    private int sommeilId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────────────────────────────
    //  CONSTRUCTEURS
    // ─────────────────────────────────────────────────────────────

    public Reve() {
        this.couleur = true;
        this.recurrent = false;
        this.intensite = 5;
        this.createdAt = LocalDateTime.now();
    }

    public Reve(String titre, String description, String humeur, String typeReve) {
        this();
        setTitre(titre);
        setDescription(description);
        setHumeur(humeur);
        setTypeReve(typeReve);
    }

    public Reve(String titre, String description, String humeur, String typeReve,
                int intensite, boolean couleur, String emotions, String symboles,
                boolean recurrent, Sommeil sommeil) {
        this();
        setTitre(titre);
        setDescription(description);
        setHumeur(humeur);
        setTypeReve(typeReve);
        setIntensite(intensite);
        setCouleur(couleur);
        setEmotions(emotions);
        setSymboles(symboles);
        setRecurrent(recurrent);
        setSommeil(sommeil);
    }

    // ─────────────────────────────────────────────────────────────
    //  NORMALISATION (anti-emoji / anti-espaces / anti-casse)
    // ─────────────────────────────────────────────────────────────

    /**
     * Nettoie un label UI comme "😱 Cauchemar" -> "cauchemar"
     * - enlève les caractères non-lettres au début (emoji, icônes)
     * - trim
     * - lowerCase (avec locale pour accents FR)
     */
    private static String normLabel(String s) {
        if (s == null) return "";
        String t = s.trim();
        // retire tout ce qui n'est pas une lettre au début (emoji, symboles, etc.)
        t = t.replaceFirst("^[^A-Za-zÀ-ÿ]+", "").trim();
        return t.toLowerCase(Locale.FRENCH);
    }

    /**
     * Nettoie une liste texte (émotions/symboles) en séparant par virgule,
     * en trim, en supprimant les vides, et en supprimant les doublons.
     */
    private static List<String> splitCleanDistinct(String s) {
        if (s == null || s.trim().isEmpty()) return List.of();
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    //  TYPES & HUMEURS (robustes même avec emojis)
    // ─────────────────────────────────────────────────────────────

    public boolean estCauchemar() {
        String t = normLabel(typeReve);
        return t.contains("cauchemar");
    }

    public boolean estLucide() {
        String t = normLabel(typeReve);
        return t.contains("lucide");
    }

    public boolean estRecurrentType() {
        String t = normLabel(typeReve);
        return t.contains("récurrent") || t.contains("recurrent");
    }

    public boolean estNormal() {
        String t = normLabel(typeReve);
        return t.contains("normal");
    }

    public boolean humeurAnxieuseOuEffrayee() {
        String h = normLabel(humeur);
        return h.contains("anxieux") || h.contains("anxieuse")
                || h.contains("effray") || h.contains("peur");
    }

    public boolean humeurTriste() {
        String h = normLabel(humeur);
        return h.contains("triste");
    }

    public boolean humeurNeutre() {
        String h = normLabel(humeur);
        return h.contains("neutre");
    }

    public boolean humeurJoyeuse() {
        String h = normLabel(humeur);
        return h.contains("joyeux") || h.contains("joyeuse")
                || h.contains("excité") || h.contains("excite")
                || h.contains("paisible");
    }

    // ─────────────────────────────────────────────────────────────
    //  ANXIÉTÉ (cohérente avec emojis + type)
    // ─────────────────────────────────────────────────────────────

    /**
     * Score anxiété 0..10 basé sur:
     * - cauchemar => +5
     * - humeur anxieux/effrayé => +3, triste => +2
     * - intensité >= 8 => +2
     */
    public int calculerNiveauAnxiete() {
        int niveau = 0;

        if (estCauchemar()) niveau += 5;

        if (humeurAnxieuseOuEffrayee()) niveau += 3;
        else if (humeurTriste()) niveau += 2;

        if (intensite >= 8) niveau += 2;

        return Math.min(10, niveau);
    }

    // ─────────────────────────────────────────────────────────────
    //  LISTES ÉMOTIONS / SYMBOLES
    // ─────────────────────────────────────────────────────────────

    public List<String> getEmotionsList() {
        return splitCleanDistinct(emotions);
    }

    public List<String> getSymbolesList() {
        return splitCleanDistinct(symboles);
    }

    public void ajouterEmotion(String emotion) {
        String e = emotion == null ? "" : emotion.trim();
        if (e.isEmpty()) return;

        List<String> list = new ArrayList<>(getEmotionsList());
        if (list.stream().noneMatch(x -> x.equalsIgnoreCase(e))) {
            list.add(e);
        }
        this.emotions = String.join(", ", list);
    }

    public void ajouterSymbole(String symbole) {
        String s = symbole == null ? "" : symbole.trim();
        if (s.isEmpty()) return;

        List<String> list = new ArrayList<>(getSymbolesList());
        if (list.stream().noneMatch(x -> x.equalsIgnoreCase(s))) {
            list.add(s);
        }
        this.symboles = String.join(", ", list);
    }

    // ─────────────────────────────────────────────────────────────
    //  TEXTE / RAPPORT
    // ─────────────────────────────────────────────────────────────

    public String genererResume() {
        String resume = (titre != null && !titre.isBlank()) ? titre : "Sans titre";
        resume += " (" + (typeReve != null ? typeReve : "—") + ")";

        if (estCauchemar()) resume += " ⚠️";
        else if (estLucide()) resume += " ✨";

        return resume;
    }

    public String genererRapportDetaille() {
        StringBuilder rapport = new StringBuilder();
        rapport.append("=== Rapport de Rêve ===\n");
        rapport.append("Titre: ").append(titre != null ? titre : "—").append("\n");
        rapport.append("Type: ").append(typeReve != null ? typeReve : "—").append("\n");
        rapport.append("Intensité: ").append(intensite).append("/10\n");
        rapport.append("Humeur: ").append(humeur != null ? humeur : "—").append("\n");
        rapport.append("En couleur: ").append(couleur ? "Oui" : "Non").append("\n");
        rapport.append("Récurrent: ").append(recurrent ? "Oui" : "Non").append("\n");
        rapport.append("Niveau d'anxiété: ").append(calculerNiveauAnxiete()).append("/10\n");

        if (emotions != null && !emotions.isBlank()) {
            rapport.append("Émotions: ").append(emotions).append("\n");
        }
        if (symboles != null && !symboles.isBlank()) {
            rapport.append("Symboles: ").append(symboles).append("\n");
        }

        rapport.append("\nDescription:\n").append(description != null ? description : "—");
        return rapport.toString();
    }

    // ─────────────────────────────────────────────────────────────
    //  VALIDATION
    // ─────────────────────────────────────────────────────────────

    public boolean estValide() {
        return titre != null && !titre.trim().isEmpty()
                && description != null && !description.trim().isEmpty()
                && intensite >= 1 && intensite <= 10
                && typeReve != null && !typeReve.trim().isEmpty()
                && humeur != null && !humeur.trim().isEmpty()
                && sommeilId > 0;
    }

    // ─────────────────────────────────────────────────────────────
    //  GETTERS / SETTERS
    // ─────────────────────────────────────────────────────────────

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }

    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getHumeur() { return humeur; }

    public void setHumeur(String humeur) { this.humeur = humeur; }

    public String getTypeReve() { return typeReve; }

    public void setTypeReve(String typeReve) { this.typeReve = typeReve; }

    public int getIntensite() { return intensite; }

    public void setIntensite(int intensite) {
        if (intensite < 1 || intensite > 10) {
            throw new IllegalArgumentException("L'intensité doit être entre 1 et 10");
        }
        this.intensite = intensite;
    }

    public boolean isCouleur() { return couleur; }

    public void setCouleur(boolean couleur) { this.couleur = couleur; }

    public String getEmotions() { return emotions; }

    public void setEmotions(String emotions) {
        // garde tel quel, mais tu pourrais aussi normaliser espaces
        this.emotions = (emotions != null && emotions.isBlank()) ? null : emotions;
    }

    public String getSymboles() { return symboles; }

    public void setSymboles(String symboles) {
        this.symboles = (symboles != null && symboles.isBlank()) ? null : symboles;
    }

    public boolean isRecurrent() { return recurrent; }

    public void setRecurrent(boolean recurrent) { this.recurrent = recurrent; }

    public Sommeil getSommeil() { return sommeil; }

    public void setSommeil(Sommeil sommeil) {
        if (this.sommeil != null && this.sommeil != sommeil) {
            this.sommeil.retirerReve(this);
        }

        this.sommeil = sommeil;

        if (sommeil != null) {
            this.sommeilId = sommeil.getId();
            if (!sommeil.getReves().contains(this)) {
                sommeil.ajouterReve(this);
            }
        } else {
            this.sommeilId = 0;
        }
    }

    public int getSommeilId() { return sommeilId; }

    public void setSommeilId(int sommeilId) { this.sommeilId = sommeilId; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // ─────────────────────────────────────────────────────────────
    //  EQUALS / HASHCODE / TOSTRING
    // ─────────────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reve)) return false;
        Reve reve = (Reve) o;
        return id == reve.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reve{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", typeReve='" + typeReve + '\'' +
                ", intensite=" + intensite +
                ", sommeilId=" + sommeilId +
                '}';
    }
}