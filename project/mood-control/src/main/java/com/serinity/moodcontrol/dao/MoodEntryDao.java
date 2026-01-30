package com.serinity.moodcontrol.dao;

import com.serinity.moodcontrol.model.MoodEntry;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class MoodEntryDao {

    // ---------------- SAVE ----------------

    public long save(MoodEntry entry) throws SQLException {
        try (Connection cn = DbConnection.getConnection()) {
            cn.setAutoCommit(false);

            try {
                long entryId = insertMoodEntry(cn, entry);

                List<Integer> emotionIds = idsByNames(cn, "emotion", entry.getEmotions());
                linkMany(cn, "mood_entry_emotion", "emotion_id", entryId, emotionIds);

                List<Integer> influenceIds = idsByNames(cn, "influence", entry.getInfluences());
                linkMany(cn, "mood_entry_influence", "influence_id", entryId, influenceIds);

                cn.commit();
                entry.setId(entryId);
                return entryId;

            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private long insertMoodEntry(Connection cn, MoodEntry entry) throws SQLException {
        String sql = "INSERT INTO mood_entry (user_id, moment_type, mood_level) VALUES (?, ?, ?)";

        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, entry.getUserId());
            ps.setString(2, entry.getMomentType());
            ps.setInt(3, entry.getMoodLevel());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
                throw new SQLException("Failed to retrieve generated id for mood_entry");
            }
        }
    }

    private List<Integer> idsByNames(Connection cn, String table, List<String> names) throws SQLException {
        if (names == null || names.isEmpty()) return Collections.emptyList();

        String placeholders = String.join(",", Collections.nCopies(names.size(), "?"));
        String sql = "SELECT id FROM " + table + " WHERE name IN (" + placeholders + ")";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < names.size(); i++) {
                ps.setString(i + 1, names.get(i));
            }

            List<Integer> ids = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("id"));
            }
            return ids;
        }
    }

    private void linkMany(Connection cn, String joinTable, String rightIdCol,
                          long moodEntryId, List<Integer> rightIds) throws SQLException {
        if (rightIds == null || rightIds.isEmpty()) return;

        String sql = "INSERT INTO " + joinTable + " (mood_entry_id, " + rightIdCol + ") VALUES (?, ?)";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (Integer id : rightIds) {
                ps.setLong(1, moodEntryId);
                ps.setInt(2, id);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // ---------------- READ HISTORY ----------------

    public List<MoodHistoryItem> findHistory(long userId, Integer lastDays, String typeFilter) throws SQLException {

        StringBuilder sql = new StringBuilder(
                "SELECT id, entry_date, moment_type, mood_level " +
                        "FROM mood_entry WHERE user_id = ? "
        );

        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (lastDays != null) {
            sql.append(" AND entry_date >= (NOW() - INTERVAL ? DAY) ");
            params.add(lastDays);
        }

        if (typeFilter != null && !"All".equalsIgnoreCase(typeFilter)) {
            String dbType;
            if ("Moment".equalsIgnoreCase(typeFilter)) dbType = "MOMENT";
            else if ("Day".equalsIgnoreCase(typeFilter)) dbType = "DAY";
            else dbType = null;

            if (dbType != null) {
                sql.append(" AND moment_type = ? ");
                params.add(dbType);
            }
        }

        sql.append(" ORDER BY entry_date DESC ");

        Map<Long, MoodHistoryItem> map = new LinkedHashMap<>();

        try (Connection cn = DbConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Long) ps.setLong(i + 1, (Long) p);
                else if (p instanceof Integer) ps.setInt(i + 1, (Integer) p);
                else ps.setString(i + 1, String.valueOf(p));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    LocalDateTime dt = rs.getTimestamp("entry_date").toLocalDateTime();
                    String momentType = rs.getString("moment_type");
                    int moodLevel = rs.getInt("mood_level");

                    map.put(id, new MoodHistoryItem(id, dt, momentType, moodLevel));
                }
            }
        }

        if (map.isEmpty()) return new ArrayList<>();

        fillEmotions(map);
        fillInfluences(map);

        return new ArrayList<>(map.values());
    }

    // ---------------- READ ONE (FOR EDIT) ----------------
    // ✅ method: findById(long moodEntryId, long userId)

    public MoodHistoryItem findById(long moodEntryId, long userId) throws SQLException {

        String sql =
                "SELECT id, entry_date, moment_type, mood_level " +
                        "FROM mood_entry " +
                        "WHERE id = ? AND user_id = ?";

        Map<Long, MoodHistoryItem> map = new LinkedHashMap<>();

        try (Connection cn = DbConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setLong(1, moodEntryId);
            ps.setLong(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    LocalDateTime dt = rs.getTimestamp("entry_date").toLocalDateTime();
                    String momentType = rs.getString("moment_type");
                    int moodLevel = rs.getInt("mood_level");

                    map.put(id, new MoodHistoryItem(id, dt, momentType, moodLevel));
                }
            }
        }

        if (map.isEmpty()) return null;

        fillEmotions(map);
        fillInfluences(map);

        return map.values().iterator().next();
    }

    // ---------------- UPDATE (REAL) ----------------
    // ✅ updates mood_entry + refreshes join tables

    public void update(MoodEntry entry) throws SQLException {
        if (entry == null) throw new IllegalArgumentException("entry is null");
        if (entry.getId() <= 0) throw new IllegalArgumentException("entry id is missing");
        if (entry.getUserId() <= 0) throw new IllegalArgumentException("user id is missing");

        try (Connection cn = DbConnection.getConnection()) {
            cn.setAutoCommit(false);

            try {
                // main row
                String sql =
                        "UPDATE mood_entry " +
                                "SET moment_type = ?, mood_level = ?, updated_at = CURRENT_TIMESTAMP " +
                                "WHERE id = ? AND user_id = ?";

                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setString(1, entry.getMomentType());
                    ps.setInt(2, entry.getMoodLevel());
                    ps.setLong(3, entry.getId());
                    ps.setLong(4, entry.getUserId());

                    int rows = ps.executeUpdate();
                    if (rows == 0) {
                        throw new SQLException("Update failed (no rows). Wrong id/user?");
                    }
                }

                // clear old links
                deleteLinks(cn, "mood_entry_emotion", entry.getId());
                deleteLinks(cn, "mood_entry_influence", entry.getId());

                // insert new links
                List<Integer> emotionIds = idsByNames(cn, "emotion", entry.getEmotions());
                linkMany(cn, "mood_entry_emotion", "emotion_id", entry.getId(), emotionIds);

                List<Integer> influenceIds = idsByNames(cn, "influence", entry.getInfluences());
                linkMany(cn, "mood_entry_influence", "influence_id", entry.getId(), influenceIds);

                cn.commit();

            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private void deleteLinks(Connection cn, String joinTable, long moodEntryId) throws SQLException {
        String sql = "DELETE FROM " + joinTable + " WHERE mood_entry_id = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, moodEntryId);
            ps.executeUpdate();
        }
    }

    // ---------------- FILL EMOTIONS / INFLUENCES ----------------

    private void fillEmotions(Map<Long, MoodHistoryItem> map) throws SQLException {
        String in = makeInClause(map.size());
        String sql =
                "SELECT mee.mood_entry_id, e.name " +
                        "FROM mood_entry_emotion mee " +
                        "JOIN emotion e ON e.id = mee.emotion_id " +
                        "WHERE mee.mood_entry_id IN " + in + " " +
                        "ORDER BY e.name";

        try (Connection con = DbConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int idx = 1;
            for (Long id : map.keySet()) ps.setLong(idx++, id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long moodEntryId = rs.getLong("mood_entry_id");
                    String name = rs.getString("name");
                    MoodHistoryItem item = map.get(moodEntryId);
                    if (item != null) item.getEmotions().add(name);
                }
            }
        }
    }

    private void fillInfluences(Map<Long, MoodHistoryItem> map) throws SQLException {
        String in = makeInClause(map.size());
        String sql =
                "SELECT mei.mood_entry_id, i.name " +
                        "FROM mood_entry_influence mei " +
                        "JOIN influence i ON i.id = mei.influence_id " +
                        "WHERE mei.mood_entry_id IN " + in + " " +
                        "ORDER BY i.name";

        try (Connection con = DbConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int idx = 1;
            for (Long id : map.keySet()) ps.setLong(idx++, id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long moodEntryId = rs.getLong("mood_entry_id");
                    String name = rs.getString("name");
                    MoodHistoryItem item = map.get(moodEntryId);
                    if (item != null) item.getInfluences().add(name);
                }
            }
        }
    }

    private String makeInClause(int n) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < n; i++) {
            sb.append("?");
            if (i < n - 1) sb.append(",");
        }
        sb.append(")");
        return sb.toString();
    }

    // ---------------- DELETE ----------------
    // ✅ signature is delete(id, userId)

    public boolean delete(long moodEntryId, long userId) throws SQLException {
        String sql = "DELETE FROM mood_entry WHERE id = ? AND user_id = ?";

        try (Connection cn = DbConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setLong(1, moodEntryId);
            ps.setLong(2, userId);

            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }
}
