package com.serinity.accesscontrol.interfaces;

import com.serinity.accesscontrol.dao.MoodHistoryItem;
import com.serinity.accesscontrol.model.MoodEntry;

import java.sql.SQLException;
import java.util.List;

public interface IMoodEntryService {

  long add(MoodEntry entry) throws SQLException;

  List<MoodHistoryItem> findHistory(String userId, Integer lastDays, String typeFilter) throws SQLException;

  MoodHistoryItem getByID(long moodEntryId, String userId) throws SQLException;

  void update(MoodEntry entry) throws SQLException;

  boolean delete(long moodEntryId, String userId) throws SQLException;
}
