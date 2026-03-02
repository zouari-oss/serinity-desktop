package com.serinity.sleepcontrol.model;

public class NlpResultat {
    private String sentimentLabel;
    private double sentimentScore;
    private String recommandations; // ← NOUVEAU

    public String getSentimentLabel()            { return sentimentLabel; }
    public void   setSentimentLabel(String v)    { sentimentLabel = v; }
    public double getSentimentScore()            { return sentimentScore; }
    public void   setSentimentScore(double v)    { sentimentScore = v; }
    public String getRecommandations()           { return recommandations; }
    public void   setRecommandations(String v)   { recommandations = v; }

    public boolean isPositif() { return "positive".equalsIgnoreCase(sentimentLabel); }
    public boolean isNegatif() { return "negative".equalsIgnoreCase(sentimentLabel); }

    public String getSentimentFr() {
        if (sentimentLabel == null) return "Inconnu";
        return switch (sentimentLabel.toLowerCase()) {
            case "positive" -> "Positif 😊";
            case "negative" -> "Négatif 😟";
            default         -> "Neutre 😐";
        };
    }

    public String getEmoji() {
        return isPositif() ? "😊" : isNegatif() ? "😟" : "😐";
    }

    public String getCouleur() {
        return isPositif() ? "#4CAF50" : isNegatif() ? "#F44336" : "#FF9800";
    }
}
