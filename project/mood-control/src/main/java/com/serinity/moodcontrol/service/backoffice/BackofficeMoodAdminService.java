package com.serinity.moodcontrol.service.backoffice;

import com.serinity.moodcontrol.dao.backoffice.BackofficeMoodAdminDao;
import com.serinity.moodcontrol.dto.backoffice.BackofficeMoodRow;

import java.sql.SQLException;
import java.util.List;

public class BackofficeMoodAdminService {

    private final BackofficeMoodAdminDao dao = new BackofficeMoodAdminDao();

    public List<BackofficeMoodRow> listMood(String userIdOrNull) throws SQLException {
        return dao.findAll(userIdOrNull);
    }

    public boolean deleteMood(long moodEntryId) throws SQLException {
        return dao.delete(moodEntryId);
    }
}