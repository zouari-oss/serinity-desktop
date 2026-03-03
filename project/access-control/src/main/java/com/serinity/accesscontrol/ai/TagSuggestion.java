package com.serinity.accesscontrol.ai;

public final class TagSuggestion {
  private final String tag;
  private final double score;

  public TagSuggestion(String tag, double score) {
    this.tag = tag;
    this.score = score;
  }

  public String getTag() {
    return tag;
  }

  public double getScore() {
    return score;
  }
}
