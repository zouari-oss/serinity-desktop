package com.serinity.accesscontrol.model;

import java.util.ArrayList;
import java.util.List;

public class MoodEntry {

  private long id;

  // UUID (CHAR(36))
  private String userId;

  // DB columns: moment_type, mood_level
  private String momentType; // "MOMENT" or "DAY"
  private int moodLevel;

  // codes (ex: CALM, HAPPY, SOCIAL_MEDIA...)
  private List<String> emotions = new ArrayList<>();
  private List<String> influences = new ArrayList<>();

  public MoodEntry() {
  }

  public MoodEntry(String userId, String momentType, int moodLevel) {
    this.userId = userId;
    this.momentType = momentType;
    this.moodLevel = moodLevel;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getMomentType() {
    return momentType;
  }

  public void setMomentType(String momentType) {
    this.momentType = momentType;
  }

  public int getMoodLevel() {
    return moodLevel;
  }

  public void setMoodLevel(int moodLevel) {
    this.moodLevel = moodLevel;
  }

  public List<String> getEmotions() {
    return emotions;
  }

  public void setEmotions(List<String> emotions) {
    this.emotions = (emotions == null) ? new ArrayList<>() : emotions;
  }

  public List<String> getInfluences() {
    return influences;
  }

  public void setInfluences(List<String> influences) {
    this.influences = (influences == null) ? new ArrayList<>() : influences;
  }
}
