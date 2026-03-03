package com.serinity.accesscontrol.model;

import java.time.LocalDateTime;

public class JournalEntry {

  private long id;
  private String userId; // UUID (CHAR(36))
  private String title;
  private String content;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // AI
  private String aiTags;
  private String aiModelVersion;
  private LocalDateTime aiGeneratedAt;

  public JournalEntry() {
  }

  public JournalEntry(String userId, String title, String content) {
    this.userId = userId;
    this.title = title;
    this.content = content;
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

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getAiTags() {
    return aiTags;
  }

  public void setAiTags(String aiTags) {
    this.aiTags = aiTags;
  }

  public String getAiModelVersion() {
    return aiModelVersion;
  }

  public void setAiModelVersion(String aiModelVersion) {
    this.aiModelVersion = aiModelVersion;
  }

  public LocalDateTime getAiGeneratedAt() {
    return aiGeneratedAt;
  }

  public void setAiGeneratedAt(LocalDateTime aiGeneratedAt) {
    this.aiGeneratedAt = aiGeneratedAt;
  }
}
