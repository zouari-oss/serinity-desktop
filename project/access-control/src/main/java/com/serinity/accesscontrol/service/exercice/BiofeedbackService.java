package com.serinity.accesscontrol.service.exercice;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public class BiofeedbackService {

    public record BioPoint(int bpm, int stress100) {}

    private final Random rnd = new Random();
    private int baseBpm = 72;

    private final Deque<Integer> last = new ArrayDeque<>();
    private double phaseEffect = 0.0;

    public void setPhaseEffect(double effect) {
        this.phaseEffect = effect;
    }

    public BioPoint next() {
        int noise = rnd.nextInt(5) - 2; // [-2..+2]
        int spike = (rnd.nextDouble() < 0.08) ? rnd.nextInt(12) : 0;

        int bpm = (int)Math.round(baseBpm + noise + spike + phaseEffect);
        bpm = Math.max(50, Math.min(140, bpm));

        last.addLast(bpm);
        while (last.size() > 30) last.removeFirst();

        int stress = computeStress(bpm);
        return new BioPoint(bpm, stress);
    }

    private int computeStress(int bpm) {
        double bpmScore = (bpm - 55) / (140.0 - 55.0);
        bpmScore = clamp01(bpmScore) * 70.0;

        double var = variabilityStd();
        double varScore = clamp01((var - 2.0) / 10.0) * 30.0;

        int s = (int)Math.round(bpmScore + varScore);
        return Math.max(0, Math.min(100, s));
    }

    private double variabilityStd() {
        if (last.size() < 5) return 0.0;
        double mean = last.stream().mapToInt(x -> x).average().orElse(0.0);
        double sum = 0.0;
        for (int x : last) sum += (x - mean) * (x - mean);
        return Math.sqrt(sum / last.size());
    }

    private double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}