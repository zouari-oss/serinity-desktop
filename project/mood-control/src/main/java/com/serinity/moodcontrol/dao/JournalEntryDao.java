package com.serinity.moodcontrol.dao;

import com.serinity.moodcontrol.model.JournalEntry;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JournalEntryDao {

    private LocalDateTime toLdt(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }

    public List<JournalEntry> findAll(long userId) throws SQLException {
        String sql = "SELECT id, user_id, title, content, created_at, updated_at " +
                "FROM journal_entry WHERE user_id=? ORDER BY created_at DESC";

        List<JournalEntry> out = new ArrayList<JournalEntry>();

        try (Connection cn = DbConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JournalEntry e = new JournalEntry();
                    e.setId(rs.getLong("id"));
                    e.setUserId(rs.getLong("user_id"));
                    e.setTitle(rs.getString("title"));
                    e.setContent(rs.getString("content"));
                    e.setCreatedAt(toLdt(rs.getTimestamp("created_at")));
                    e.setUpdatedAt(toLdt(rs.getTimestamp("updated_at")));
                    out.add(e);
                }
            }
        }
        return out;
    }

    public JournalEntry findById(long id, long userId) throws SQLException {
        String sql = "SELECT id, user_id, title, content, created_at, updated_at " +
                "FROM journal_entry WHERE id=? AND user_id=?";

        try (Connection cn = DbConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.setLong(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                JournalEntry e = new JournalEntry();
                e.setId(rs.getLong("id"));
                e.setUserId(rs.getLong("user_id"));
                e.setTitle(rs.getString("title"));
                e.setContent(rs.getString("content"));
                e.setCreatedAt(toLdt(rs.getTimestamp("created_at")));
                e.setUpdatedAt(toLdt(rs.getTimestamp("updated_at")));
                return e;
            }
        }
    }

    public long insert(JournalEntry e) throws SQLException {
        String sql = "INSERT INTO journal_entry(user_id, title, content) VALUES(?,?,?)";

        try (Connection cn = DbConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, e.getUserId());
            ps.setString(2, e.getTitle());
            ps.setString(3, e.getContent());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        }
        return -1L;
    }

    public boolean update(JournalEntry e) throws SQLException {
        String sql = "UPDATE journal_entry SET title=?, content=? WHERE id=? AND user_id=?";

        try (Connection cn = DbConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, e.getTitle());
            ps.setString(2, e.getContent());
            ps.setLong(3, e.getId());
            ps.setLong(4, e.getUserId());

            return ps.executeUpdate() == 1;
        }
    }

    public boolean delete(long id, long userId) throws SQLException {
        String sql = "DELETE FROM journal_entry WHERE id=? AND user_id=?";

        try (Connection cn = DbConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.setLong(2, userId);

            return ps.executeUpdate() == 1;
        }
    }
}
