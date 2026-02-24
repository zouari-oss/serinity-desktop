package com.serinity.sleepcontrol.api;

public class WeatherData {

    private double  temperature;
    private double  temperatureRessentie;
    private double  humidite;
    private double  vitesseVent;
    private String  description;
    private String  icone;
    private String  ville;
    private boolean chargee  = false;
    private String  erreur   = null;

    // ─── Getters / Setters ───────────────────────────────────────────────────────

    public double  getTemperature()            { return temperature; }
    public void    setTemperature(double v)    { this.temperature = v; }

    public double  getTemperatureRessentie()         { return temperatureRessentie; }
    public void    setTemperatureRessentie(double v) { this.temperatureRessentie = v; }

    public double  getHumidite()               { return humidite; }
    public void    setHumidite(double v)       { this.humidite = v; }

    public double  getVitesseVent()            { return vitesseVent; }
    public void    setVitesseVent(double v)    { this.vitesseVent = v; }

    public String  getDescription()            { return description; }
    public void    setDescription(String v)    { this.description = v; }

    public String  getIcone()                  { return icone; }
    public void    setIcone(String v)          { this.icone = v; }

    public String  getVille()                  { return ville; }
    public void    setVille(String v)          { this.ville = v; }

    public boolean isChargee()                 { return chargee; }
    public void    setChargee(boolean v)       { this.chargee = v; }

    public String  getErreur()                 { return erreur; }
    public void    setErreur(String v)         { this.erreur = v; }

    // ─── Impact sommeil ──────────────────────────────────────────────────────────

    public String evaluerImpactSommeil() {
        StringBuilder sb = new StringBuilder();

        if (temperature > 28)
            sb.append("🌡️ Température élevée — sommeil difficile. ");
        else if (temperature < 10)
            sb.append("🥶 Température basse — prévoir couverture. ");
        else
            sb.append("✅ Température favorable au sommeil. ");

        if (humidite > 70)
            sb.append("💧 Humidité forte — peut perturber le sommeil.");
        else if (humidite < 30)
            sb.append("🏜️ Air trop sec — pense à t'hydrater.");

        return sb.toString().trim();
    }

    public String getConseilSommeil() {
        if (temperature > 28)
            return "💡 Ventilateur ou climatisation recommandés.";
        if (temperature < 10)
            return "💡 Chambre bien isolée et couverture chaude.";
        if (humidite > 70)
            return "💡 Ouvre la fenêtre pour ventiler avant de dormir.";
        if (vitesseVent > 40)
            return "💡 Vent fort — ferme bien les fenêtres.";
        return "💡 Conditions idéales pour une bonne nuit.";
    }

    public String getNiveauAlerte() {
        if (temperature > 35 || vitesseVent > 60 || humidite > 85)
            return "ÉLEVÉ";
        if (temperature > 28 || vitesseVent > 40 || humidite > 70)
            return "MODÉRÉ";
        return "AUCUN";
    }
}
