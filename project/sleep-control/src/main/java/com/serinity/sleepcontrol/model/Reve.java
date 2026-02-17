package com.serinity.sleepcontrol.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Entité Reve représentant un rêve
 * Relation ManyToOne avec Sommeil
 */
public class Reve {

    // Attributs principaux
    private int id;
    private String titre;
    private String description;
    private String humeur; // Joyeux, Triste, Anxieux, Neutre, Excité
    private String typeReve; // Normal, Cauchemar, Lucide, Récurrent

    // Nouveaux attributs
    private int intensite; // 1-10
    private boolean couleur; // Rêve en couleur ou noir & blanc
    private String emotions; // Liste séparée par virgules
    private String symboles; // Symboles présents dans le rêve
    private boolean recurrent; // Si le rêve se répète

    // Relation ManyToOne avec Sommeil
    private Sommeil sommeil;
    private int sommeilId; // Foreign Key

    // Métadonnées
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== CONSTRUCTEURS ====================

    public Reve() {
        this.couleur = true;
        this.recurrent = false;
        this.intensite = 5;
        this.createdAt = LocalDateTime.now();
    }

    public Reve(String titre, String description, String humeur, String typeReve) {
        this();
        this.titre = titre;
        this.description = description;
        this.humeur = humeur;
        this.typeReve = typeReve;
    }

    public Reve(String titre, String description, String humeur, String typeReve,
                int intensite, boolean couleur, String emotions, String symboles,
                boolean recurrent, Sommeil sommeil) {
        this();
        this.titre = titre;
        this.description = description;
        this.humeur = humeur;
        this.typeReve = typeReve;
        this.intensite = intensite;
        this.couleur = couleur;
        this.emotions = emotions;
        this.symboles = symboles;
        this.recurrent = recurrent;
        setSommeil(sommeil);
    }

    // ==================== MÉTHODES MÉTIER ====================

    /**
     * Vérifie si le rêve est un cauchemar
     */
    public boolean estCauchemar() {
        return "Cauchemar".equalsIgnoreCase(typeReve);
    }

    /**
     * Vérifie si le rêve est lucide
     */
    public boolean estLucide() {
        return "Lucide".equalsIgnoreCase(typeReve);
    }

    /**
     * Évalue le niveau d'anxiété du rêve (0-10)
     */
    public int calculerNiveauAnxiete() {
        int niveau = 0;

        if (estCauchemar()) {
            niveau += 5;
        }

        if (humeur != null) {
            if (humeur.equalsIgnoreCase("Anxieux") || humeur.equalsIgnoreCase("Effrayé")) {
                niveau += 3;
            } else if (humeur.equalsIgnoreCase("Triste")) {
                niveau += 2;
            }
        }

        if (intensite >= 8) {
            niveau += 2;
        }

        return Math.min(10, niveau);
    }

    /**
     * Retourne la liste des émotions sous forme de List
     */
    public List<String> getEmotionsList() {
        if (emotions == null || emotions.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(emotions.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * Retourne la liste des symboles sous forme de List
     */
    public List<String> getSymbolesList() {
        if (symboles == null || symboles.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(symboles.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * Ajoute une émotion à la liste
     */
    public void ajouterEmotion(String emotion) {
        if (emotion != null && !emotion.trim().isEmpty()) {
            if (this.emotions == null || this.emotions.isEmpty()) {
                this.emotions = emotion;
            } else if (!getEmotionsList().contains(emotion)) {
                this.emotions += ", " + emotion;
            }
        }
    }

    /**
     * Ajoute un symbole à la liste
     */
    public void ajouterSymbole(String symbole) {
        if (symbole != null && !symbole.trim().isEmpty()) {
            if (this.symboles == null || this.symboles.isEmpty()) {
                this.symboles = symbole;
            } else if (!getSymbolesList().contains(symbole)) {
                this.symboles += ", " + symbole;
            }
        }
    }

    /**
     * Génère un résumé court du rêve
     */
    public String genererResume() {
        String resume = titre != null ? titre : "Sans titre";
        resume += " (" + typeReve + ")";
        if (estCauchemar()) {
            resume += " ⚠️";
        } else if (estLucide()) {
            resume += " ✨";
        }
        return resume;
    }

    /**
     * Génère un rapport détaillé du rêve
     */
    public String genererRapportDetaille() {
        StringBuilder rapport = new StringBuilder();
        rapport.append("=== Rapport de Rêve ===\n");
        rapport.append("Titre: ").append(titre).append("\n");
        rapport.append("Type: ").append(typeReve).append("\n");
        rapport.append("Intensité: ").append(intensite).append("/10\n");
        rapport.append("Humeur: ").append(humeur).append("\n");
        rapport.append("En couleur: ").append(couleur ? "Oui" : "Non").append("\n");
        rapport.append("Récurrent: ").append(recurrent ? "Oui" : "Non").append("\n");
        rapport.append("Niveau d'anxiété: ").append(calculerNiveauAnxiete()).append("/10\n");

        if (emotions != null && !emotions.isEmpty()) {
            rapport.append("Émotions: ").append(emotions).append("\n");
        }

        if (symboles != null && !symboles.isEmpty()) {
            rapport.append("Symboles: ").append(symboles).append("\n");
        }

        rapport.append("\nDescription:\n").append(description);

        return rapport.toString();
    }

    /**
     * Valide les données du rêve
     */
    public boolean estValide() {
        return titre != null && !titre.trim().isEmpty() &&
                description != null && !description.trim().isEmpty() &&
                intensite >= 1 && intensite <= 10 &&
                typeReve != null && !typeReve.trim().isEmpty();
    }

    // ==================== GETTERS & SETTERS ====================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHumeur() {
        return humeur;
    }

    public void setHumeur(String humeur) {
        this.humeur = humeur;
    }

    public String getTypeReve() {
        return typeReve;
    }

    public void setTypeReve(String typeReve) {
        this.typeReve = typeReve;
    }

    public int getIntensite() {
        return intensite;
    }

    public void setIntensite(int intensite) {
        if (intensite < 1 || intensite > 10) {
            throw new IllegalArgumentException("L'intensité doit être entre 1 et 10");
        }
        this.intensite = intensite;
    }

    public boolean isCouleur() {
        return couleur;
    }

    public void setCouleur(boolean couleur) {
        this.couleur = couleur;
    }

    public String getEmotions() {
        return emotions;
    }

    public void setEmotions(String emotions) {
        this.emotions = emotions;
    }

    public String getSymboles() {
        return symboles;
    }

    public void setSymboles(String symboles) {
        this.symboles = symboles;
    }

    public boolean isRecurrent() {
        return recurrent;
    }

    public void setRecurrent(boolean recurrent) {
        this.recurrent = recurrent;
    }

    public Sommeil getSommeil() {
        return sommeil;
    }

    public void setSommeil(Sommeil sommeil) {
        // Retirer de l'ancien sommeil si existant
        if (this.sommeil != null && this.sommeil != sommeil) {
            this.sommeil.retirerReve(this);
        }

        this.sommeil = sommeil;

        if (sommeil != null) {
            this.sommeilId = sommeil.getId();
            // Ajouter au nouveau sommeil
            if (!sommeil.getReves().contains(this)) {
                sommeil.ajouterReve(this);
            }
        } else {
            this.sommeilId = 0;
        }
    }

    public int getSommeilId() {
        return sommeilId;
    }

    public void setSommeilId(int sommeilId) {
        this.sommeilId = sommeilId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ==================== EQUALS, HASHCODE, TOSTRING ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
