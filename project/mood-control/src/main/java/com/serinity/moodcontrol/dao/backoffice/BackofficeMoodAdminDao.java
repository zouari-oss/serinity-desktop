package com.serinity.moodcontrol.dao.backoffice;

import com.serinity.moodcontrol.dto.backoffice.BackofficeMoodRow;

import com.serinity.moodcontrol.dao.DbConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Backend/admin DAO: reads/deletes mood entries across ALL users.
 * Cross-references access-control via profiles (LEFT JOIN).
 * No FK constraints assumed -> deletes join rows manually.
 */
public class BackofficeMoodAdminDao {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * @param nameLikeOrNull null/blank => all users; otherwise filters by username/first/last LIKE
     */
    public List<BackofficeMoodRow> findAll(String nameLikeOrNull) throws SQLException {
        boolean byName = nameLikeOrNull != null && !nameLikeOrNull.trim().isBlank();

        final String sql =
                "SELECT " +
                        "  me.id, me.user_id, me.moment_type, me.mood_level, me.entry_date, " +
                        "  p.username, CONCAT_WS(' ', p.firstName, p.lastName) AS full_name, " +
                        "  (SELECT GROUP_CONCAT(DISTINCT e.name ORDER BY e.name SEPARATOR ', ') " +
                        "     FROM mood_entry_emotion mee " +
                        "     JOIN emotion e ON e.id = mee.emotion_id " +
                        "    WHERE mee.mood_entry_id = me.id) AS emotions, " +
                        "  (SELECT GROUP_CONCAT(DISTINCT i.name ORDER BY i.name SEPARATOR ', ') " +
                        "     FROM mood_entry_influence mei " +
                        "     JOIN influence i ON i.id = mei.influence_id " +
                        "    WHERE mei.mood_entry_id = me.id) AS influences " +
                        "FROM mood_entry me " +
                        "LEFT JOIN profiles p ON p.user_id = me.user_id " +
                        (byName
                                ? "WHERE (p.username LIKE ? OR p.firstName LIKE ? OR p.lastName LIKE ?) "
                                : "") +
                        "ORDER BY me.entry_date DESC";

        final List<BackofficeMoodRow> out = new ArrayList<>();
        final Connection cn = DbConnection.getConnection();

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (byName) {
                String q = "%" + nameLikeOrNull.trim() + "%";
                ps.setString(1, q);
                ps.setString(2, q);
                ps.setString(3, q);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String userId = rs.getString("user_id");
                    String type = rs.getString("moment_type");
                    int level = rs.getInt("mood_level");

                    Timestamp ts = rs.getTimestamp("entry_date");
                    String entryDateTxt = format(ts);

                    String username = rs.getString("username");
                    String fullName = rs.getString("full_name");

                    String display = buildDisplay(userId, fullName, username);

                    String emotions = rs.getString("emotions");
                    String influences = rs.getString("influences");

                    out.add(new BackofficeMoodRow(
                            id,
                            userId,
                            display,
                            type,
                            level,
                            entryDateTxt,
                            emotions == null ? "" : emotions,
                            influences == null ? "" : influences
                    ));
                }
            }
        }
        return out;
    }

    /**
     * Admin delete: delete join rows first, then mood entry. Transactional, no FKs assumed.
     */
    public boolean delete(final long moodEntryId) throws SQLException {
        final Connection cn = DbConnection.getConnection();
        cn.setAutoCommit(false);

        try {
            deleteLinks(cn, "mood_entry_emotion", moodEntryId);
            deleteLinks(cn, "mood_entry_influence", moodEntryId);

            final String sql = "DELETE FROM mood_entry WHERE id = ?";
            int rows;
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setLong(1, moodEntryId);
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

    private void deleteLinks(final Connection cn, final String joinTable, final long moodEntryId) throws SQLException {
        final String sql = "DELETE FROM " + joinTable + " WHERE mood_entry_id = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, moodEntryId);
            ps.executeUpdate();
        }
    }

    private String format(Timestamp ts) {
        if (ts == null) return "";
        LocalDateTime ldt = ts.toLocalDateTime();
        return DT.format(ldt);
    }

    private String buildDisplay(String userId, String fullName, String username) {
        if (fullName != null && !fullName.isBlank()) {
            if (username != null && !username.isBlank()) return fullName + " @" + username;
            return fullName;
        }
        if (username != null && !username.isBlank()) return "@" + username;
        return userId;
    }
}