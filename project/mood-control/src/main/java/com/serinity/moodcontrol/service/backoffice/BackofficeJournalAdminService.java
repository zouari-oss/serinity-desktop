package com.serinity.moodcontrol.service.backoffice;

import com.serinity.moodcontrol.dao.backoffice.BackofficeJournalAdminDao;
import com.serinity.moodcontrol.dto.backoffice.BackofficeJournalRow;

import java.sql.SQLException;
import java.util.List;

public class BackofficeJournalAdminService {

    private final BackofficeJournalAdminDao dao = new BackofficeJournalAdminDao();

    public List<BackofficeJournalRow> listJournals(String userIdOrNull) throws SQLException {
        return dao.findAll(userIdOrNull);
    }

    public boolean deleteJournal(long journalEntryId) throws SQLException {
        return dao.delete(journalEntryId);
    }
}