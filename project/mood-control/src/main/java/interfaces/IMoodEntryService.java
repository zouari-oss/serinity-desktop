package com.serinity.moodcontrol.interfaces;

import com.serinity.moodcontrol.dao.MoodHistoryItem;
import com.serinity.moodcontrol.model.MoodEntry;

import java.sql.SQLException;
import java.util.List;

public interface IMoodEntryService {
    long add(MoodEntry entry) throws SQLException;

    List<MoodHistoryItem> findHistory(long userId, Integer lastDays, String typeFilter) throws SQLException;

    MoodHistoryItem getByID(long moodEntryId, long userId) throws SQLException;

    void update(MoodEntry entry) throws SQLException;

    boolean delete(long moodEntryId, long userId) throws SQLException;
}