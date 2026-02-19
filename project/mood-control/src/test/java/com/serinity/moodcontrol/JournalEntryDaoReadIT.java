package com.serinity.moodcontrol.dao;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

class JournalEntryDaoReadIT {

    @Test
    void canSelectFromJournalEntryTable() throws Exception {
        // This test only verifies that the query runs and the columns exist.
        // It does NOT require any rows to be present.
        try (Connection cn = DbConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(
                     "SELECT id, user_id, title, content FROM journal_entry LIMIT 1"
             );
             ResultSet rs = ps.executeQuery()) {

            // Query executed successfully if we got here.
            // If there's a row, check basic column reads work.
            if (rs.next()) {
                rs.getLong("id");
                rs.getLong("user_id");
                rs.getString("title");
                rs.getString("content");
            }
        }
    }
}
