package com.serinity.accesscontrol.model.exercice;

public class BreathingPlan {
    private final BreathingState state;
    private final BreathingProtocol protocol;
    private final String why;

    public BreathingPlan(BreathingState state, BreathingProtocol protocol, String why) {
        this.state = state;
        this.protocol = protocol;
        this.why = why;
    }

    public BreathingState getState() { return state; }
    public BreathingProtocol getProtocol() { return protocol; }
    public String getWhy() { return why; }
}