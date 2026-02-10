package com.serinity.moodcontrol.dao;

import com.serinity.moodcontrol.model.MoodEntry;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class MoodEntryDao {

  // SAVE
  public long save(final MoodEntry entry) throws SQLException {
    try (Connection cn = DbConnection.getConnection()) {
      cn.setAutoCommit(false);

      try {
        final long entryId = insertMoodEntry(cn, entry);

        // entry.getEmotions() CODE
        final List<Integer> emotionIds = idsByCodes(cn, "emotion", entry.getEmotions());
        linkMany(cn, "mood_entry_emotion", "emotion_id", entryId, emotionIds);

        // entry.getInfluences() CODES
        final List<Integer> influenceIds = idsByCodes(cn, "influence", entry.getInfluences());
        linkMany(cn, "mood_entry_influence", "influence_id", entryId, influenceIds);

        cn.commit();
        entry.setId(entryId);
        return entryId;

      } catch (final SQLException e) {
        cn.rollback();
        throw e;
      } finally {
        cn.setAutoCommit(true);
      }
    }
  }

  // HISTORY
  public List<MoodHistoryItem> findHistory(final long userId, final Integer lastDays, final String typeFilter)
      throws SQLException {

    final StringBuilder sql = new StringBuilder(
        "SELECT id, entry_date, moment_type, mood_level " +
            "FROM mood_entry WHERE user_id = ? ");

    final List<Object> params = new ArrayList<Object>();
    params.add(Long.valueOf(userId));

    if (lastDays != null) {
      sql.append(" AND entry_date >= (NOW() - INTERVAL ? DAY) ");
      params.add(Integer.valueOf(lastDays));
    }


    // typeFilter  "MOMENT" or "DAY" codes strings

    if (typeFilter != null && !"ALL".equalsIgnoreCase(typeFilter)) {
      final String dbType = typeFilter.trim().toUpperCase(Locale.ROOT);
      if ("MOMENT".equals(dbType) || "DAY".equals(dbType)) {
        sql.append(" AND moment_type = ? ");
        params.add(dbType);
      }
    }

    sql.append(" ORDER BY entry_date DESC ");

    final Map<Long, MoodHistoryItem> map = new LinkedHashMap<Long, MoodHistoryItem>();

    try (Connection cn = DbConnection.getConnection();
        PreparedStatement ps = cn.prepareStatement(sql.toString())) {

      for (int i = 0; i < params.size(); i++) {
        final Object p = params.get(i);
        if (p instanceof Long)
          ps.setLong(i + 1, ((Long) p).longValue());
        else if (p instanceof Integer)
          ps.setInt(i + 1, ((Integer) p).intValue());
        else
          ps.setString(i + 1, String.valueOf(p));
      }

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          final long id = rs.getLong("id");
          final LocalDateTime dt = rs.getTimestamp("entry_date").toLocalDateTime();
          final String momentType = rs.getString("moment_type");
          final int moodLevel = rs.getInt("mood_level");

          map.put(Long.valueOf(id), new MoodHistoryItem(id, dt, momentType, moodLevel));
        }
      }
    }

    if (map.isEmpty())
      return new ArrayList<MoodHistoryItem>();

    fillEmotions(map);
    fillInfluences(map);

    return new ArrayList<MoodHistoryItem>(map.values());
  }

  // ---------------- READ ONE (FOR EDIT) ----------------
  public MoodHistoryItem findById(final long moodEntryId, final long userId) throws SQLException {

    final String sql = "SELECT id, entry_date, moment_type, mood_level " +
        "FROM mood_entry " +
        "WHERE id = ? AND user_id = ?";

    final Map<Long, MoodHistoryItem> map = new LinkedHashMap<Long, MoodHistoryItem>();

    try (Connection cn = DbConnection.getConnection();
        PreparedStatement ps = cn.prepareStatement(sql)) {

      ps.setLong(1, moodEntryId);
      ps.setLong(2, userId);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          final long id = rs.getLong("id");
          final LocalDateTime dt = rs.getTimestamp("entry_date").toLocalDateTime();
          final String momentType = rs.getString("moment_type");
          final int moodLevel = rs.getInt("mood_level");

          map.put(Long.valueOf(id), new MoodHistoryItem(id, dt, momentType, moodLevel));
        }
      }
    }

    if (map.isEmpty())
      return null;

    fillEmotions(map);
    fillInfluences(map);

    return map.values().iterator().next();
  }

  // ---------------- UPDATE (REAL) ----------------
  public void update(final MoodEntry entry) throws SQLException {
    if (entry == null)
      throw new IllegalArgumentException("entry is null");
    if (entry.getId() <= 0)
      throw new IllegalArgumentException("entry id is missing");
    if (entry.getUserId() <= 0)
      throw new IllegalArgumentException("user id is missing");

    try (Connection cn = DbConnection.getConnection()) {
      cn.setAutoCommit(false);

      try {
        final String sql = "UPDATE mood_entry " +
            "SET moment_type = ?, mood_level = ?, updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = ? AND user_id = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
          ps.setString(1, entry.getMomentType());
          ps.setInt(2, entry.getMoodLevel());
          ps.setLong(3, entry.getId());
          ps.setLong(4, entry.getUserId());

          final int rows = ps.executeUpdate();
          if (rows == 0) {
            throw new SQLException("Update failed (no rows). Wrong id/user?");
          }
        }

        deleteLinks(cn, "mood_entry_emotion", entry.getId());
        deleteLinks(cn, "mood_entry_influence", entry.getId());

        final List<Integer> emotionIds = idsByCodes(cn, "emotion", entry.getEmotions());
        linkMany(cn, "mood_entry_emotion", "emotion_id", entry.getId(), emotionIds);

        final List<Integer> influenceIds = idsByCodes(cn, "influence", entry.getInfluences());
        linkMany(cn, "mood_entry_influence", "influence_id", entry.getId(), influenceIds);

        cn.commit();

      } catch (final SQLException e) {
        cn.rollback();
        throw e;
      } finally {
        cn.setAutoCommit(true);
      }
    }
  }

  // ---------------- DELETE ----------------
  public boolean delete(final long moodEntryId, final long userId) throws SQLException {
      try (Connection cn = DbConnection.getConnection()) {
          cn.setAutoCommit(false);

          try {
              // delete join rows first (safe even if none)
              deleteLinks(cn, "mood_entry_emotion", moodEntryId);
              deleteLinks(cn, "mood_entry_influence", moodEntryId);

              // then delete the entry (scoped to user)
              final String sql = "DELETE FROM mood_entry WHERE id = ? AND user_id = ?";
              int rows;
              try (PreparedStatement ps = cn.prepareStatement(sql)) {
                  ps.setLong(1, moodEntryId);
                  ps.setLong(2, userId);
                  rows = ps.executeUpdate();
              }

              cn.commit();
              return rows > 0;

          } catch (final SQLException e) {
              cn.rollback();
              throw e;
          } finally {
              cn.setAutoCommit(true);
          }
      }
  }

  private long insertMoodEntry(final Connection cn, final MoodEntry entry) throws SQLException {
    final String sql = "INSERT INTO mood_entry (user_id, moment_type, mood_level) VALUES (?, ?, ?)";

    try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setLong(1, entry.getUserId());
      ps.setString(2, entry.getMomentType()); // MOMENT / DAY
      ps.setInt(3, entry.getMoodLevel());
      ps.executeUpdate();

      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next())
          return rs.getLong(1);
        throw new SQLException("Failed to retrieve generated id for mood_entry");
      }
    }
  }

  /**
   * Resolve IDs by matching codes against normalized DB names.
   *
   * emotion: UPPER(name) matches CALM because "Calm" -> CALM
   * influence: UPPER(REPLACE(REPLACE(name,'/','_'),' ','_')) matches SCHOOL_WORK
   * because "School/Work" -> SCHOOL_WORK
   */
  private List<Integer> idsByCodes(final Connection cn, final String table, final List<String> codes)
      throws SQLException {
    if (codes == null || codes.isEmpty())
      return Collections.emptyList();

    final List<String> cleaned = new ArrayList<>();
    for (final String c : codes) {
      if (c == null)
        continue;
      final String v = c.trim().toUpperCase(Locale.ROOT);
      if (!v.isEmpty())
        cleaned.add(v);
    }
    if (cleaned.isEmpty())
      return Collections.emptyList();

    final String placeholders = String.join(",", Collections.nCopies(cleaned.size(), "?"));

    String nameExpr;
    if ("influence".equalsIgnoreCase(table)) {
      nameExpr = "UPPER(REPLACE(REPLACE(name,'/','_'),' ','_'))";
    } else {
      nameExpr = "UPPER(name)";
    }

    final String sql = "SELECT id FROM " + table + " WHERE " + nameExpr + " IN (" + placeholders + ")";

    try (PreparedStatement ps = cn.prepareStatement(sql)) {
      for (int i = 0; i < cleaned.size(); i++) {
        ps.setString(i + 1, cleaned.get(i));
      }

      final List<Integer> ids = new ArrayList<>();
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next())
          ids.add(rs.getInt("id"));
      }
      return ids;
    }
  }

  private void linkMany(final Connection cn, final String joinTable, final String rightIdCol,
      final long moodEntryId, final List<Integer> rightIds) throws SQLException {
    if (rightIds == null || rightIds.isEmpty())
      return;

    final String sql = "INSERT INTO " + joinTable + " (mood_entry_id, " + rightIdCol + ") VALUES (?, ?)";

    try (PreparedStatement ps = cn.prepareStatement(sql)) {
      for (final Integer id : rightIds) {
        ps.setLong(1, moodEntryId);
        ps.setInt(2, id);
        ps.addBatch();
      }
      ps.executeBatch();
    }
  }

  private void deleteLinks(final Connection cn, final String joinTable, final long moodEntryId) throws SQLException {
    final String sql = "DELETE FROM " + joinTable + " WHERE mood_entry_id = ?";
    try (PreparedStatement ps = cn.prepareStatement(sql)) {
      ps.setLong(1, moodEntryId);
      ps.executeUpdate();
    }
  }

  // ---------------- FILL EMOTIONS / INFLUENCES ----------------
  // IMPORTANT: return CODES, not English labels
  private void fillEmotions(final Map<Long, MoodHistoryItem> map) throws SQLException {
    final String in = makeInClause(map.size());
    final String sql = "SELECT mee.mood_entry_id, UPPER(e.name) AS code " +
        "FROM mood_entry_emotion mee " +
        "JOIN emotion e ON e.id = mee.emotion_id " +
        "WHERE mee.mood_entry_id IN " + in + " " +
        "ORDER BY e.name";

    try (Connection con = DbConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {

      int idx = 1;
      for (final Long id : map.keySet())
        ps.setLong(idx++, id.longValue());

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          final long moodEntryId = rs.getLong("mood_entry_id");
          final String code = rs.getString("code"); // ex: CALM
          final MoodHistoryItem item = map.get(Long.valueOf(moodEntryId));
          if (item != null)
            item.getEmotions().add(code);
        }
      }
    }
  }

  private void fillInfluences(final Map<Long, MoodHistoryItem> map) throws SQLException {
    final String in = makeInClause(map.size());
    final String sql = "SELECT mei.mood_entry_id, " +
        "UPPER(REPLACE(REPLACE(i.name,'/','_'),' ','_')) AS code " +
        "FROM mood_entry_influence mei " +
        "JOIN influence i ON i.id = mei.influence_id " +
        "WHERE mei.mood_entry_id IN " + in + " " +
        "ORDER BY i.name";

    try (Connection con = DbConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {

      int idx = 1;
      for (final Long id : map.keySet())
        ps.setLong(idx++, id.longValue());

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          final long moodEntryId = rs.getLong("mood_entry_id");
          final String code = rs.getString("code"); // ex: SOCIAL_MEDIA
          final MoodHistoryItem item = map.get(Long.valueOf(moodEntryId));
          if (item != null)
            item.getInfluences().add(code);
        }
      }
    }
  }

  private String makeInClause(final int n) {
    final StringBuilder sb = new StringBuilder("(");
    for (int i = 0; i < n; i++) {
      sb.append("?");
      if (i < n - 1)
        sb.append(",");
    }
    sb.append(")");
    return sb.toString();
  }
} // MoodEntryDao class
