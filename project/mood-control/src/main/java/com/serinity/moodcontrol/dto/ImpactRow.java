package com.serinity.moodcontrol.dto;

public final class ImpactRow {

    private final String label;
    private final double avgMood;
    private final int lowMoodCount;
    private final int totalSamples;

    public ImpactRow(String label, double avgMood, int lowMoodCount, int totalSamples) {
        this.label = label;
        this.avgMood = avgMood;
        this.lowMoodCount = lowMoodCount;
        this.totalSamples = totalSamples;
    }

    public String getLabel() { return label; }
    public double getAvgMood() { return avgMood; }
    public int getLowMoodCount() { return lowMoodCount; }
    public int getTotalSamples() { return totalSamples; }
}