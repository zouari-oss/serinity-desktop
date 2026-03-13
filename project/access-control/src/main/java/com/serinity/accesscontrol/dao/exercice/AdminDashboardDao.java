package com.serinity.accesscontrol.dao.exercice;

import com.serinity.accesscontrol.util.MyDataBase;

import com.serinity.accesscontrol.dao.exercice.ExerciseSessionDao.SessionSummary;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardDao {

    private final Connection cnx;

    public AdminDashboardDao() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    public List<SessionSummary> findRecentSessionsFiltered(
            int userId,
            int days,
            int limit,
            Integer exerciseId,
            String status,
            String feedbackContains
    ) throws SQLException {

        String sql = """
            SELECT
              id, user_id, exercise_id, status, started_at, completed_at,
              CASE
                WHEN started_at IS NOT NULL AND completed_at IS NOT NULL
                THEN TIMESTAMPDIFF(SECOND, started_at, completed_at)
                ELSE 0
              END AS active_seconds_calc,
              feedback
            FROM exercise_session
            WHERE user_id = ?
              AND ( started_at IS NULL OR started_at >= (NOW() - INTERVAL ? DAY) )
              AND ( ? IS NULL OR exercise_id = ? )
              AND ( ? IS NULL OR UPPER(status) = UPPER(?) )
              AND ( ? IS NULL OR feedback LIKE ? )
            ORDER BY COALESCE(started_at, completed_at) DESC
            LIMIT ?
        """;

        List<SessionSummary> out = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            int i = 1;
            ps.setInt(i++, userId);
            ps.setInt(i++, Math.max(1, days));

            // exercise
            if (exerciseId == null) {
                ps.setNull(i++, Types.INTEGER);
                ps.setNull(i++, Types.INTEGER);
            } else {
                ps.setInt(i++, exerciseId);
                ps.setInt(i++, exerciseId);
            }

            // status
            if (status == null || status.isBlank()) {
                ps.setNull(i++, Types.VARCHAR);
                ps.setNull(i++, Types.VARCHAR);
            } else {
                ps.setString(i++, status);
                ps.setString(i++, status);
            }

            // feedback contains
            if (feedbackContains == null || feedbackContains.isBlank()) {
                ps.setNull(i++, Types.VARCHAR);
                ps.setNull(i++, Types.VARCHAR);
            } else {
                ps.setString(i++, "%" + feedbackContains + "%");
                ps.setString(i++, "%" + feedbackContains + "%");
            }

            ps.setInt(i++, Math.max(1, limit));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new SessionSummary(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getInt("exercise_id"),
                            rs.getString("status"),
                            toLdt(rs.getTimestamp("started_at")),
                            toLdt(rs.getTimestamp("completed_at")),
                            rs.getInt("active_seconds_calc"),
                            rs.getString("feedback")
                    ));
                }
            }
        }
        return out;
    }

    public void deleteSession(int sessionId) throws SQLException {
        String sql = "DELETE FROM exercise_session WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.executeUpdate();
        }
    }

    private static LocalDateTime toLdt(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}