package com.serinity.exercicecontrol.model;

  /** Class documentation. */
public class BreathingProtocol {
    private final String name;
    private final int totalSeconds;

    // cycle en secondes
    private final int inhale;
    private final int hold1;
    private final int exhale;
    private final int hold2;

    private final String benefit;

  /** Documents BreathingProtocol. */
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

  /** Documents getName. */
    public String getName() { return name; }
  /** Documents getTotalSeconds. */
    public int getTotalSeconds() { return totalSeconds; }

  /** Documents getInhale. */
    public int getInhale() { return inhale; }
  /** Documents getHold1. */
    public int getHold1() { return hold1; }
  /** Documents getExhale. */
    public int getExhale() { return exhale; }
  /** Documents getHold2. */
    public int getHold2() { return hold2; }

  /** Documents getBenefit. */
    public String getBenefit() { return benefit; }

  /** Documents cycleSeconds. */
    public int cycleSeconds() {
        return inhale + hold1 + exhale + hold2;
    }
}
