package com.serinity.sleepcontrol.ia;

import java.util.List;
import java.util.Map;

public class AnalyseResult {

    private int scorePsychologique;
    private String profilDominant;
    private Map<String, String> symbolesDetectes;
    private String impactEmotionnel;
    private String conclusion;
    private List<String> recommandations;
    private String niveauAlerte;

    public int getScorePsychologique()               { return scorePsychologique; }
    public void setScorePsychologique(int s)         { this.scorePsychologique = s; }

    public String getProfilDominant()                { return profilDominant; }
    public void setProfilDominant(String p)          { this.profilDominant = p; }

    public Map<String, String> getSymbolesDetectes() { return symbolesDetectes; }
    public void setSymbolesDetectes(Map<String, String> s) { this.symbolesDetectes = s; }

    public String getImpactEmotionnel()              { return impactEmotionnel; }
    public void setImpactEmotionnel(String i)        { this.impactEmotionnel = i; }

    public String getConclusion()                    { return conclusion; }
    public void setConclusion(String c)              { this.conclusion = c; }

    public List<String> getRecommandations()         { return recommandations; }
    public void setRecommandations(List<String> r)   { this.recommandations = r; }

    public String getNiveauAlerte()                  { return niveauAlerte; }
    public void setNiveauAlerte(String n)            { this.niveauAlerte = n; }
}
