package com.serinity.moodcontrol;

import com.serinity.moodcontrol.dao.DbConnection;
import com.serinity.moodcontrol.dao.JournalEntryDao;
import com.serinity.moodcontrol.model.JournalEntry;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JournalEntryDaoIT {

    // Use valid UUID-like values (CHAR(36))
    private static final String USER_1 = "00000000-0000-0000-0000-000000000001";
    private static final String USER_2 = "00000000-0000-0000-0000-000000000002";

    private final JournalEntryDao dao = new JournalEntryDao();

    @BeforeEach
    void clean() throws SQLException {
        // Only delete rows created by these tests (scoped by our test user ids)
        try (Connection cn = DbConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(
                     "DELETE FROM journal_entry WHERE user_id IN (?, ?)"
             )) {
            ps.setString(1, USER_1);
            ps.setString(2, USER_2);
            ps.executeUpdate();
        }
    }

    @Test
    @Order(1)
    void insert_thenFindById_returnsRow() throws SQLException {
        JournalEntry e = new JournalEntry();
        e.setUserId(USER_1);
        e.setTitle("IT Title");
        e.setContent("IT Content");

        long id = dao.insert(e);
        assertTrue(id > 0, "insert should return generated id (not -1)");

        JournalEntry loaded = dao.findById(id, USER_1);
        assertNotNull(loaded);
        assertEquals(id, loaded.getId());
        assertEquals(USER_1, loaded.getUserId());
        assertEquals("IT Title", loaded.getTitle());
        assertEquals("IT Content", loaded.getContent());
    }

    @Test
    @Order(2)
    void findAll_scopedByUserId_onlyReturnsThatUsersRows() throws SQLException {
        JournalEntry e1 = new JournalEntry();
        e1.setUserId(USER_1);
        e1.setTitle("U1");
        e1.setContent("C1");
        dao.insert(e1);

        JournalEntry e2 = new JournalEntry();
        e2.setUserId(USER_2);
        e2.setTitle("U2");
        e2.setContent("C2");
        dao.insert(e2);

        List<JournalEntry> u1 = dao.findAll(USER_1);
        List<JournalEntry> u2 = dao.findAll(USER_2);

        assertEquals(1, u1.size());
        assertEquals(1, u2.size());
        assertEquals(USER_1, u1.get(0).getUserId());
        assertEquals(USER_2, u2.get(0).getUserId());
    }

    @Test
    @Order(3)
    void update_requiresMatchingUserId() throws SQLException {
        JournalEntry e = new JournalEntry();
        e.setUserId(USER_1);
        e.setTitle("Original");
        e.setContent("Original content");
        long id = dao.insert(e);

        // Wrong user tries update
        JournalEntry attempt = new JournalEntry();
        attempt.setId(id);
        attempt.setUserId(USER_2);
        attempt.setTitle("Hacked");
        attempt.setContent("Hacked content");

        assertFalse(dao.update(attempt), "update must fail when user_id doesn't match");

        // Verify still original
        JournalEntry loaded = dao.findById(id, USER_1);
        assertNotNull(loaded);
        assertEquals("Original", loaded.getTitle());
        assertEquals("Original content", loaded.getContent());
    }

    @Test
    @Order(4)
    void delete_requiresMatchingUserId() throws SQLException {
        JournalEntry e = new JournalEntry();
        e.setUserId(USER_1);
        e.setTitle("To delete");
        e.setContent("...");
        long id = dao.insert(e);

        assertFalse(dao.delete(id, USER_2), "delete must fail when user_id doesn't match");
        assertTrue(dao.delete(id, USER_1), "delete must succeed for correct user_id");
        assertNull(dao.findById(id, USER_1));
    }
}