package com.serinity.accesscontrol.service.backoffice;

import com.serinity.accesscontrol.dao.backoffice.BackofficeMoodAdminDao;
import com.serinity.accesscontrol.dto.backoffice.BackofficeMoodRow;

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
