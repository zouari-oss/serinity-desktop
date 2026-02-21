package com.serinity.moodcontrol.interfaces;

import com.serinity.moodcontrol.model.JournalEntry;

import java.sql.SQLException;
import java.util.List;

public interface IJournalEntryService {
    long add(JournalEntry entry) throws SQLException;

    List<JournalEntry> getAll(long userId) throws SQLException;

    JournalEntry getByID(long id, long userId) throws SQLException;

    boolean update(JournalEntry entry) throws SQLException;

    boolean delete(long id, long userId) throws SQLException;
}