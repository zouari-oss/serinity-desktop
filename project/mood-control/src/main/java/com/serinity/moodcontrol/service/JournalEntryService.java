package com.serinity.moodcontrol.service;

import com.serinity.moodcontrol.dao.JournalEntryDao;
import com.serinity.moodcontrol.interfaces.IJournalEntryService;
import com.serinity.moodcontrol.model.JournalEntry;

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
    public List<JournalEntry> getAll(long userId) throws SQLException {
        return dao.findAll(userId);
    }

    @Override
    public JournalEntry getByID(long id, long userId) throws SQLException {
        return dao.findById(id, userId);
    }

    @Override
    public boolean update(JournalEntry entry) throws SQLException {
        Objects.requireNonNull(entry, "entry");
        return dao.update(entry);
    }

    @Override
    public boolean delete(long id, long userId) throws SQLException {
        return dao.delete(id, userId);
    }
}