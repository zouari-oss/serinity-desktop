package com.serinity.moodcontrol.interfaces;

import com.serinity.moodcontrol.dto.ImpactRow;

import java.sql.SQLException;
import java.util.List;

public interface IMoodAnalyticsDao {

    List<ImpactRow> findInfluenceImpact(
            String userId,
            Integer lastDays,
            String typeFilter,      // "ALL" | "DAY" | "MOMENT"
            int lowMoodThreshold,   //  2
            int minSamples,         //  3
            int limit               //  5
    ) throws SQLException;

    List<ImpactRow> findEmotionImpact(
            String userId,
            Integer lastDays,
            String typeFilter,
            int lowMoodThreshold,
            int minSamples,
            int limit
    ) throws SQLException;
}