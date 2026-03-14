package com.serinity.exercicecontrol.model;

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

  /** Documents ActionPlan. */
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

  /** Documents getPlannedMinutes. */
    public int getPlannedMinutes() { return plannedMinutes; }
  /** Documents getPlannedTime. */
    public LocalTime getPlannedTime() { return plannedTime; }

  /** Documents getMicroCommitment. */
    public String getMicroCommitment() { return microCommitment; }

  /** Documents getStreakDays. */
    public int getStreakDays() { return streakDays; }
  /** Documents getStreakMessage. */
    public String getStreakMessage() { return streakMessage; }

  /** Documents plannedTimeLabel. */
    public String plannedTimeLabel() {
        if (plannedTime == null) return "—";
        return String.format("%02d:%02d", plannedTime.getHour(), plannedTime.getMinute());
    }
}
