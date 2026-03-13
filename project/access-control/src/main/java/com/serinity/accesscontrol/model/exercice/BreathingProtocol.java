package com.serinity.accesscontrol.model.exercice;

public class BreathingProtocol {
    private final String name;
    private final int totalSeconds;

    // cycle en secondes
    private final int inhale;
    private final int hold1;
    private final int exhale;
    private final int hold2;

    private final String benefit;

    public BreathingProtocol(String name, int totalSeconds,
                             int inhale, int hold1, int exhale, int hold2,
                             String benefit) {
        this.name = name;
        this.totalSeconds = totalSeconds;
        this.inhale = inhale;
        this.hold1 = hold1;
        this.exhale = exhale;
        this.hold2 = hold2;
        this.benefit = benefit;
    }

    public String getName() { return name; }
    public int getTotalSeconds() { return totalSeconds; }

    public int getInhale() { return inhale; }
    public int getHold1() { return hold1; }
    public int getExhale() { return exhale; }
    public int getHold2() { return hold2; }

    public String getBenefit() { return benefit; }

    public int cycleSeconds() {
        return inhale + hold1 + exhale + hold2;
    }
}