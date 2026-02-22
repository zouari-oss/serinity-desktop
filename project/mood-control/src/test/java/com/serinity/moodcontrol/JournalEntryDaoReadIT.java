package com.serinity.moodcontrol;

import com.serinity.moodcontrol.dao.DbConnection;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

class JournalEntryDaoReadIT {

    @Test
    void canSelectFromJournalEntryTable() throws Exception {
        try (Connection cn = DbConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(
                     "SELECT id, user_id, title, content FROM journal_entry LIMIT 1"
             );
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                rs.getLong("id");
                rs.getString("user_id"); // UUID now
                rs.getString("title");
                rs.getString("content");
            }
        }
    }
}