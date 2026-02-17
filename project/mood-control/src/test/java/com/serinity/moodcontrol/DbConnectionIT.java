package com.serinity.moodcontrol.dao;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

class DbConnectionIT {

    @Test
    void getConnection_returnsOpenConnection_andMetadata() throws Exception {
        try (Connection cn = DbConnection.getConnection()) {
            assertNotNull(cn);
            assertFalse(cn.isClosed());

            DatabaseMetaData md = cn.getMetaData();
            assertNotNull(md);

            // These are safe + read-only checks
            assertNotNull(md.getDatabaseProductName());
            assertNotNull(md.getDatabaseProductVersion());
            assertNotNull(md.getDriverName());
        }
    }

    @Test
    void journalEntryTable_exists() throws Exception {
        try (Connection cn = DbConnection.getConnection()) {
            DatabaseMetaData md = cn.getMetaData();

            // table name casing can vary; try common cases
            assertTrue(
                    tableExists(md, "journal_entry") ||
                            tableExists(md, "JOURNAL_ENTRY") ||
                            tableExists(md, "Journal_Entry"),
                    "Expected table journal_entry to exist in the current schema"
            );
        }
    }

    private boolean tableExists(DatabaseMetaData md, String table) throws Exception {
        try (ResultSet rs = md.getTables(null, null, table, new String[]{"TABLE"})) {
            return rs.next();
        }
    }
}
