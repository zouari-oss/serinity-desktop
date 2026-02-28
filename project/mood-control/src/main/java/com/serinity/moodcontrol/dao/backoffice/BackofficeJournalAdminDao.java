package com.serinity.moodcontrol.dao.backoffice;

import com.serinity.moodcontrol.dto.backoffice.BackofficeJournalRow;

import com.serinity.moodcontrol.dao.DbConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Backend/admin DAO: reads/deletes journal entries across ALL users.
 * Cross-references access-control via profiles (LEFT JOIN).
 */
public class BackofficeJournalAdminDao {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * @param nameLikeOrNull null/blank => all users; otherwise filters by username/first/last LIKE
     */
    public List<BackofficeJournalRow> findAll(String nameLikeOrNull) throws SQLException {
        boolean byName = nameLikeOrNull != null && !nameLikeOrNull.trim().isBlank();

        final String sql =
                "SELECT je.id, je.user_id, je.title, je.created_at, je.updated_at, " +
                        "       p.username, CONCAT_WS(' ', p.firstName, p.lastName) AS full_name " +
                        "FROM journal_entry je " +
                        "LEFT JOIN profiles p ON p.user_id = je.user_id " +
                        (byName
                                ? "WHERE (p.username LIKE ? OR p.firstName LIKE ? OR p.lastName LIKE ?) "
                                : "") +
                        "ORDER BY je.created_at DESC";

        final List<BackofficeJournalRow> out = new ArrayList<>();
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
                    String title = rs.getString("title");

                    String createdTxt = format(rs.getTimestamp("created_at"));
                    String updatedTxt = format(rs.getTimestamp("updated_at"));

                    String username = rs.getString("username");
                    String fullName = rs.getString("full_name");

                    String display = buildDisplay(userId, fullName, username);

                    out.add(new BackofficeJournalRow(
                            id,
                            userId,
                            display,
                            title,
                            createdTxt,
                            updatedTxt
                    ));
                }
            }
        }
        return out;
    }

    public boolean delete(final long journalEntryId) throws SQLException {
        final Connection cn = DbConnection.getConnection();
        cn.setAutoCommit(false);

        try {
            final String sql = "DELETE FROM journal_entry WHERE id = ?";
            int rows;
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setLong(1, journalEntryId);
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