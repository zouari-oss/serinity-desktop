package com.serinity.accesscontrol.service;

import com.serinity.accesscontrol.dao.MoodAnalyticsDao;
import com.serinity.accesscontrol.dto.ImpactReport;
import com.serinity.accesscontrol.dto.ImpactRow;
import com.serinity.accesscontrol.interfaces.IMoodAnalyticsDao;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class MoodAnalyticsService {

  private final IMoodAnalyticsDao dao;

  public MoodAnalyticsService() {
    this.dao = new MoodAnalyticsDao();
  }

  public MoodAnalyticsService(IMoodAnalyticsDao dao) {
    this.dao = Objects.requireNonNull(dao);
  }

  public ImpactReport getImpactReport(String userId, Integer lastDays, String typeFilter) throws SQLException {
    int lowMoodThreshold = 2;
    int minSamples = 3;
    int limit = 5;

    List<ImpactRow> influences = dao.findInfluenceImpact(
        userId, lastDays, typeFilter, lowMoodThreshold, minSamples, limit);

    List<ImpactRow> emotions = dao.findEmotionImpact(
        userId, lastDays, typeFilter, lowMoodThreshold, minSamples, limit);

    return new ImpactReport(influences, emotions);
  }
}
