package com.serinity.moodcontrol.dao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MoodHistoryItem {
  private final long id;
  private final LocalDateTime dateTime;
  private final String momentType;
  private final int moodLevel;

  private final List<String> emotions = new ArrayList<>();
  private final List<String> influences = new ArrayList<>();

  public MoodHistoryItem(final long id, final LocalDateTime dateTime, final String momentType, final int moodLevel) {
    this.id = id;
    this.dateTime = dateTime;
    this.momentType = momentType;
    this.moodLevel = moodLevel;
  }

  public long getId() {
    return id;
  }

  public LocalDateTime getDateTime() {
    return dateTime;
  }

  public String getMomentType() {
    return momentType;
  }

  public int getMoodLevel() {
    return moodLevel;
  }

  public List<String> getEmotions() {
    return emotions;
  }

  public List<String> getInfluences() {
    return influences;
  }
}
