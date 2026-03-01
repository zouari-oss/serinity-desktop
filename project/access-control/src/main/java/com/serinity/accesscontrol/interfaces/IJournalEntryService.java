package com.serinity.accesscontrol.interfaces;

import com.serinity.accesscontrol.model.JournalEntry;
import java.sql.SQLException;
import java.util.List;

public interface IJournalEntryService {
  long add(JournalEntry entry) throws SQLException;

  List<JournalEntry> getAll(String userId) throws SQLException;

  JournalEntry getByID(long id, String userId) throws SQLException;

  boolean update(JournalEntry entry) throws SQLException;

  boolean delete(long id, String userId) throws SQLException;
}
