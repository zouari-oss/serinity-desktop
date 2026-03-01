package com.serinity.accesscontrol.dao;

import com.serinity.accesscontrol.model.JournalEntry;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JournalEntryDao {

  private LocalDateTime toLdt(final Timestamp ts) {
    return ts == null ? null : ts.toLocalDateTime();
  }

  public List<JournalEntry> findAll(final String userId) throws SQLException {
    Objects.requireNonNull(userId, "userId");

    final String sql = "SELECT id, user_id, title, content, created_at, updated_at, " +
        "ai_tags, ai_model_version, ai_generated_at " +
        "FROM journal_entry WHERE user_id=? ORDER BY created_at DESC";

    final List<JournalEntry> out = new ArrayList<>();

    final Connection cn = DbConnection.getConnection(); // DO NOT close (singleton)
    try (PreparedStatement ps = cn.prepareStatement(sql)) {

      ps.setString(1, userId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          final JournalEntry e = new JournalEntry();
          e.setId(rs.getLong("id"));
          e.setUserId(rs.getString("user_id"));
          e.setTitle(rs.getString("title"));
          e.setContent(rs.getString("content"));
          e.setCreatedAt(toLdt(rs.getTimestamp("created_at")));
          e.setUpdatedAt(toLdt(rs.getTimestamp("updated_at")));

          // AI fields
          e.setAiTags(rs.getString("ai_tags"));
          e.setAiModelVersion(rs.getString("ai_model_version"));
          e.setAiGeneratedAt(toLdt(rs.getTimestamp("ai_generated_at")));

          out.add(e);
        }
      }
    }

    return out;
  }

  public JournalEntry findById(final long id, final String userId) throws SQLException {
    Objects.requireNonNull(userId, "userId");

    final String sql = "SELECT id, user_id, title, content, created_at, updated_at, " +
        "ai_tags, ai_model_version, ai_generated_at " +
        "FROM journal_entry WHERE id=? AND user_id=?";

    final Connection cn = DbConnection.getConnection(); // DO NOT close (singleton)
    try (PreparedStatement ps = cn.prepareStatement(sql)) {

      ps.setLong(1, id);
      ps.setString(2, userId);

      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next())
          return null;

        final JournalEntry e = new JournalEntry();
        e.setId(rs.getLong("id"));
        e.setUserId(rs.getString("user_id"));
        e.setTitle(rs.getString("title"));
        e.setContent(rs.getString("content"));
        e.setCreatedAt(toLdt(rs.getTimestamp("created_at")));
        e.setUpdatedAt(toLdt(rs.getTimestamp("updated_at")));

        // AI fields
        e.setAiTags(rs.getString("ai_tags"));
        e.setAiModelVersion(rs.getString("ai_model_version"));
        e.setAiGeneratedAt(toLdt(rs.getTimestamp("ai_generated_at")));

        return e;
      }
    }
  }

  public long insert(final JournalEntry e) throws SQLException {
    Objects.requireNonNull(e, "entry");
    Objects.requireNonNull(e.getUserId(), "entry.userId");

    final String sql = "INSERT INTO journal_entry(user_id, title, content) VALUES(?,?,?)";

    final Connection cn = DbConnection.getConnection(); // DO NOT close (singleton)
    try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      ps.setString(1, e.getUserId());
      ps.setString(2, e.getTitle());
      ps.setString(3, e.getContent());

      ps.executeUpdate();

      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next())
          return keys.getLong(1);
      }
    }

    return -1L;
  }

  public boolean update(final JournalEntry e) throws SQLException {
    Objects.requireNonNull(e, "entry");
    Objects.requireNonNull(e.getUserId(), "entry.userId");

    final String sql = "UPDATE journal_entry SET title=?, content=? WHERE id=? AND user_id=?";

    final Connection cn = DbConnection.getConnection(); // DO NOT close (singleton)
    try (PreparedStatement ps = cn.prepareStatement(sql)) {

      ps.setString(1, e.getTitle());
      ps.setString(2, e.getContent());
      ps.setLong(3, e.getId());
      ps.setString(4, e.getUserId());

      return ps.executeUpdate() == 1;
    }
  }

  public boolean updateAiFields(
      final long id,
      final String userId,
      final String aiTagsJson,
      final String aiModelVersion,
      final LocalDateTime aiGeneratedAt) throws SQLException {

    Objects.requireNonNull(userId, "userId");

    final String sql = "UPDATE journal_entry " +
        "SET ai_tags=?, ai_model_version=?, ai_generated_at=? " +
        "WHERE id=? AND user_id=?";

    final Connection cn = DbConnection.getConnection(); // DO NOT close (singleton)
    try (PreparedStatement ps = cn.prepareStatement(sql)) {
      ps.setString(1, aiTagsJson);
      ps.setString(2, aiModelVersion);
      if (aiGeneratedAt == null)
        ps.setTimestamp(3, null);
      else
        ps.setTimestamp(3, Timestamp.valueOf(aiGeneratedAt));
      ps.setLong(4, id);
      ps.setString(5, userId);

      return ps.executeUpdate() == 1;
    }
  }

  public boolean delete(final long id, final String userId) throws SQLException {
    Objects.requireNonNull(userId, "userId");

    final String sql = "DELETE FROM journal_entry WHERE id=? AND user_id=?";

    final Connection cn = DbConnection.getConnection(); // DO NOT close (singleton)
    try (PreparedStatement ps = cn.prepareStatement(sql)) {

      ps.setLong(1, id);
      ps.setString(2, userId);

      return ps.executeUpdate() == 1;
    }
  }
}
