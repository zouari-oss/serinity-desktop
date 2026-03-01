package com.serinity.accesscontrol.service;

import com.serinity.accesscontrol.dao.MoodEntryDao;
import com.serinity.accesscontrol.dao.MoodHistoryItem;
import com.serinity.accesscontrol.interfaces.IMoodEntryService;
import com.serinity.accesscontrol.model.MoodEntry;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class MoodEntryService implements IMoodEntryService {

  private final MoodEntryDao dao;

  public MoodEntryService() {
    this.dao = new MoodEntryDao();
  }

  public MoodEntryService(MoodEntryDao dao) {
    this.dao = Objects.requireNonNull(dao);
  }

  @Override
  public long add(MoodEntry entry) throws SQLException {
    Objects.requireNonNull(entry, "entry");
    return dao.save(entry);
  }

  @Override
  public List<MoodHistoryItem> findHistory(String userId, Integer lastDays, String typeFilter) throws SQLException {
    Objects.requireNonNull(userId, "userId");
    return dao.findHistory(userId, lastDays, typeFilter);
  }

  @Override
  public MoodHistoryItem getByID(long moodEntryId, String userId) throws SQLException {
    Objects.requireNonNull(userId, "userId");
    return dao.findById(moodEntryId, userId);
  }

  @Override
  public void update(MoodEntry entry) throws SQLException {
    Objects.requireNonNull(entry, "entry");
    dao.update(entry);
  }

  @Override
  public boolean delete(long moodEntryId, String userId) throws SQLException {
    Objects.requireNonNull(userId, "userId");
    return dao.delete(moodEntryId, userId);
  }
}
