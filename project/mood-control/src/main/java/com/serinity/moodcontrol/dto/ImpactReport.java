package com.serinity.moodcontrol.dto;

import java.util.List;

public final class ImpactReport {

    private final List<ImpactRow> influences;
    private final List<ImpactRow> emotions;

    public ImpactReport(List<ImpactRow> influences, List<ImpactRow> emotions) {
        this.influences = influences;
        this.emotions = emotions;
    }

    public List<ImpactRow> getInfluences() { return influences; }
    public List<ImpactRow> getEmotions() { return emotions; }
}