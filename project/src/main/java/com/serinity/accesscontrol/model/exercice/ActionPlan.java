package com.serinity.accesscontrol.model.exercice;

import java.time.LocalTime;

/**
 * Plan d'action (action planning) + tâche graduée (graded task) + motivation.
 */
public class ActionPlan {
    private final int plannedMinutes;
    private final LocalTime plannedTime;

    private final String microCommitment;

    // ✅ streak intelligent
    private final int streakDays;
    private final String streakMessage;

    public ActionPlan(int plannedMinutes,
                      LocalTime plannedTime,
                      String microCommitment,
                      int streakDays,
                      String streakMessage) {
        this.plannedMinutes = plannedMinutes;
        this.plannedTime = plannedTime;
        this.microCommitment = microCommitment;
        this.streakDays = streakDays;
        this.streakMessage = streakMessage;
    }

    public int getPlannedMinutes() { return plannedMinutes; }
    public LocalTime getPlannedTime() { return plannedTime; }

    public String getMicroCommitment() { return microCommitment; }

    public int getStreakDays() { return streakDays; }
    public String getStreakMessage() { return streakMessage; }

    public String plannedTimeLabel() {
        if (plannedTime == null) return "—";
        return String.format("%02d:%02d", plannedTime.getHour(), plannedTime.getMinute());
    }
}