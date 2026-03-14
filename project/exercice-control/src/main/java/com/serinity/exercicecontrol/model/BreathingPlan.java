package com.serinity.exercicecontrol.model;

  /** Class documentation. */
public class BreathingPlan {
    private final BreathingState state;
    private final BreathingProtocol protocol;
    private final String why;

  /** Documents BreathingPlan. */
    public BreathingPlan(BreathingState state, BreathingProtocol protocol, String why) {
        this.state = state;
        this.protocol = protocol;
        this.why = why;
    }

  /** Documents getState. */
    public BreathingState getState() { return state; }
  /** Documents getProtocol. */
    public BreathingProtocol getProtocol() { return protocol; }
  /** Documents getWhy. */
    public String getWhy() { return why; }
}
