package com.serinity.moodcontrol.model;

import java.util.ArrayList;
import java.util.List;

public class MoodEntry {

    private long id;          // db generated
    private long userId = 1;  // TEMP until Users module

    private String momentType; // "MOMENT" or "DAY"
    private int moodLevel;     // 1..5

    private List<String> emotions = new ArrayList<>();
    private List<String> influences = new ArrayList<>();

    public MoodEntry() {}

    public MoodEntry(long userId, String momentType, int moodLevel,
                     List<String> emotions, List<String> influences) {
        this.userId = userId;
        this.momentType = momentType;
        this.moodLevel = moodLevel;
        if (emotions != null) this.emotions = emotions;
        if (influences != null) this.influences = influences;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getMomentType() { return momentType; }
    public void setMomentType(String momentType) { this.momentType = momentType; }

    public int getMoodLevel() { return moodLevel; }
    public void setMoodLevel(int moodLevel) { this.moodLevel = moodLevel; }

    public List<String> getEmotions() { return emotions; }
    public void setEmotions(List<String> emotions) { this.emotions = emotions; }

    public List<String> getInfluences() { return influences; }
    public void setInfluences(List<String> influences) { this.influences = influences; }
}
