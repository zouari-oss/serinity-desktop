package com.serinity.accesscontrol.service;

import com.serinity.accesscontrol.dao.JournalEntryDao;
import com.serinity.accesscontrol.interfaces.IJournalEntryService;
import com.serinity.accesscontrol.model.JournalEntry;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class JournalEntryService implements IJournalEntryService {

  private final JournalEntryDao dao;

  public JournalEntryService() {
    this.dao = new JournalEntryDao();
  }

  public JournalEntryService(JournalEntryDao dao) {
    this.dao = Objects.requireNonNull(dao);
  }

  @Override
  public long add(JournalEntry entry) throws SQLException {
    Objects.requireNonNull(entry, "entry");
    return dao.insert(entry);
  }

  @Override
  public List<JournalEntry> getAll(String userId) throws SQLException {
    Objects.requireNonNull(userId, "userId");
    return dao.findAll(userId);
  }

  @Override
  public JournalEntry getByID(long id, String userId) throws SQLException {
    Objects.requireNonNull(userId, "userId");
    return dao.findById(id, userId);
  }

  @Override
  public boolean update(JournalEntry entry) throws SQLException {
    Objects.requireNonNull(entry, "entry");
    return dao.update(entry);
  }

  @Override
  public boolean delete(long id, String userId) throws SQLException {
    Objects.requireNonNull(userId, "userId");
    return dao.delete(id, userId);
  }
}
