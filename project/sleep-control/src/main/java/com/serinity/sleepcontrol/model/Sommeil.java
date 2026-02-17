package com.serinity.sleepcontrol.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Sommeil {

    private int id;
    private LocalDate dateNuit;
    private LocalTime heureCoucher;
    private LocalTime heureReveil;
    private String qualite;
    private String commentaire;

    private double dureeSommeil;
    private int interruptions;
    private String humeurReveil;
    private String environnement;
    private double temperature;
    private String niveauBruit;

    private List<Reve> reves;

    public Sommeil() {
        this.reves = new ArrayList<>();
        this.interruptions = 0;
        this.temperature = 20.0;
    }

    public Sommeil(LocalDate dateNuit, LocalTime heureCoucher, LocalTime heureReveil,
                   String qualite) {
        this();
        this.dateNuit = dateNuit;
        this.heureCoucher = heureCoucher;
        this.heureReveil = heureReveil;
        this.qualite = qualite;
        this.dureeSommeil = calculerDuree();
    }

    public Sommeil(LocalDate dateNuit, LocalTime heureCoucher, LocalTime heureReveil,
                   String qualite, String commentaire, int interruptions,
                   String humeurReveil, String environnement, double temperature,
                   String niveauBruit) {
        this();
        this.dateNuit = dateNuit;
        this.heureCoucher = heureCoucher;
        this.heureReveil = heureReveil;
        this.qualite = qualite;
        this.commentaire = commentaire;
        this.interruptions = interruptions;
        this.humeurReveil = humeurReveil;
        this.environnement = environnement;
        this.temperature = temperature;
        this.niveauBruit = niveauBruit;
        this.dureeSommeil = calculerDuree();
    }

    public double calculerDuree() {
        if (heureCoucher != null && heureReveil != null) {
            Duration duration;
            if (heureReveil.isBefore(heureCoucher)) {
                duration = Duration.between(heureCoucher, heureReveil.plusHours(24));
            } else {
                duration = Duration.between(heureCoucher, heureReveil);
            }
            return Math.round(duration.toMinutes() / 60.0 * 100.0) / 100.0;
        }
        return 0;
    }

    public void ajouterReve(Reve reve) {
        if (reve != null && !reves.contains(reve)) {
            reves.add(reve);
            reve.setSommeil(this);
        }
    }

    public void retirerReve(Reve reve) {
        if (reve != null && reves.contains(reve)) {
            reves.remove(reve);
            reve.setSommeil(null);
        }
    }

    public int getNombreReves() {
        return reves.size();
    }

    public boolean estDureeOptimale() {
        return dureeSommeil >= 7.0 && dureeSommeil <= 9.0;
    }

    public int calculerScoreQualite() {
        int score = 50;

        if (dureeSommeil >= 7 && dureeSommeil <= 9) {
            score += 20;
        } else if (dureeSommeil >= 6 && dureeSommeil < 7) {
            score += 10;
        } else if (dureeSommeil < 5) {
            score -= 20;
        }

        if (interruptions == 0) {
            score += 15;
        } else if (interruptions <= 2) {
            score += 5;
        } else {
            score -= interruptions * 3;
        }

        switch (qualite.toLowerCase()) {
            case "excellente":
                score += 15;
                break;
            case "bonne":
                score += 10;
                break;
            case "moyenne":
                score += 0;
                break;
            case "mauvaise":
                score -= 10;
                break;
        }

        return Math.max(0, Math.min(100, score));
    }

    public String genererRapport() {
        StringBuilder rapport = new StringBuilder();
        rapport.append("=== Rapport de Sommeil ===\n");
        rapport.append("Date: ").append(dateNuit.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        rapport.append("Coucher: ").append(heureCoucher).append(" - Réveil: ").append(heureReveil).append("\n");
        rapport.append("Durée: ").append(String.format("%.2f", dureeSommeil)).append(" heures\n");
        rapport.append("Qualité: ").append(qualite).append("\n");
        rapport.append("Interruptions: ").append(interruptions).append("\n");
        rapport.append("Score: ").append(calculerScoreQualite()).append("/100\n");
        rapport.append("Nombre de rêves: ").append(getNombreReves()).append("\n");
        return rapport.toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDateNuit() {
        return dateNuit;
    }

    public void setDateNuit(LocalDate dateNuit) {
        this.dateNuit = dateNuit;
    }

    public LocalTime getHeureCoucher() {
        return heureCoucher;
    }

    public void setHeureCoucher(LocalTime heureCoucher) {
        this.heureCoucher = heureCoucher;
        this.dureeSommeil = calculerDuree();
    }

    public LocalTime getHeureReveil() {
        return heureReveil;
    }

    public void setHeureReveil(LocalTime heureReveil) {
        this.heureReveil = heureReveil;
        this.dureeSommeil = calculerDuree();
    }

    public String getQualite() {
        return qualite;
    }

    public void setQualite(String qualite) {
        this.qualite = qualite;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public double getDureeSommeil() {
        return dureeSommeil;
    }

    public void setDureeSommeil(double dureeSommeil) {
        this.dureeSommeil = dureeSommeil;
    }

    public int getInterruptions() {
        return interruptions;
    }

    public void setInterruptions(int interruptions) {
        this.interruptions = Math.max(0, interruptions);
    }

    public String getHumeurReveil() {
        return humeurReveil;
    }

    public void setHumeurReveil(String humeurReveil) {
        this.humeurReveil = humeurReveil;
    }

    public String getEnvironnement() {
        return environnement;
    }

    public void setEnvironnement(String environnement) {
        this.environnement = environnement;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getNiveauBruit() {
        return niveauBruit;
    }

    public void setNiveauBruit(String niveauBruit) {
        this.niveauBruit = niveauBruit;
    }

    public List<Reve> getReves() {
        return new ArrayList<>(reves);
    }

    public void setReves(List<Reve> reves) {
        this.reves = reves != null ? new ArrayList<>(reves) : new ArrayList<>();
        for (Reve reve : this.reves) {
            if (reve.getSommeil() != this) {
                reve.setSommeil(this);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sommeil sommeil = (Sommeil) o;
        return id == sommeil.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Sommeil{" +
                "id=" + id +
                ", dateNuit=" + dateNuit +
                ", qualite='" + qualite + '\'' +
                ", dureeSommeil=" + String.format("%.2f", dureeSommeil) + "h" +
                ", interruptions=" + interruptions +
                ", nbReves=" + getNombreReves() +
                '}';
    }
}
