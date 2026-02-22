package com.serinity.exercicecontrol.dao;

import com.serinity.exercicecontrol.service.SessionStatus;

import java.sql.*;
import java.time.LocalDateTime;

public class SessionDAO {

    // =============================
    // CREATE (CREATED session)
    // =============================
    public int createCreatedSession(int userId, int exerciseId) throws SQLException {
        String sql = """
            INSERT INTO exercise_session
                (user_id, exercise_id, status, started_at, completed_at, feedback, active_seconds, last_resumed_at)
            VALUES
                (?, ?, 'CREATED', NULL, NULL, NULL, 0, NULL)
        """;

        Connection cnx = DbConnection.getInstance().getConnection();

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setInt(2, exerciseId);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        return -1;
    }

    // =============================
    // READ (FOR UPDATE)
    // =============================
    public SessionEntity findByIdForUpdate(int sessionId) throws SQLException {
        String sql = """
            SELECT id, status, started_at, completed_at, feedback, active_seconds, last_resumed_at
            FROM exercise_session
            WHERE id = ?
            FOR UPDATE
        """;

        Connection cnx = DbConnection.getInstance().getConnection();

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new SessionEntity(
                        rs.getInt("id"),
                        SessionStatus.fromDb(rs.getString("status")),
                        toLdt(rs.getTimestamp("started_at")),
                        toLdt(rs.getTimestamp("completed_at")),
                        rs.getString("feedback"),
                        rs.getInt("active_seconds"),
                        toLdt(rs.getTimestamp("last_resumed_at"))
                );
            }
        }
    }

    // =============================
    // UPDATE
    // =============================
    public void update(SessionEntity s) throws SQLException {
        String sql = """
            UPDATE exercise_session
            SET status = ?,
                started_at = ?,
                completed_at = ?,
                feedback = ?,
                active_seconds = ?,
                last_resumed_at = ?
            WHERE id = ?
        """;

        Connection cnx = DbConnection.getInstance().getConnection();

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, s.status().name());
            ps.setTimestamp(2, toTs(s.startedAt()));
            ps.setTimestamp(3, toTs(s.completedAt()));
            ps.setString(4, s.feedback());
            ps.setInt(5, s.activeSeconds());
            ps.setTimestamp(6, toTs(s.lastResumedAt()));
            ps.setInt(7, s.id());
            ps.executeUpdate();
        }
    }

    // =============================
    // TRANSACTION WRAPPER
    // =============================
    public void withTransaction(SqlRunnable block) throws SQLException {
        Connection cnx = DbConnection.getInstance().getConnection();
        boolean oldAuto = cnx.getAutoCommit();

        cnx.setAutoCommit(false);
        try {
            block.run();
            cnx.commit();
        } catch (SQLException e) {
            cnx.rollback();
            throw e;
        } finally {
            cnx.setAutoCommit(oldAuto);
        }
    }

    // =============================
    // Helpers
    // =============================
    private static LocalDateTime toLdt(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }

    private static Timestamp toTs(LocalDateTime ldt) {
        return ldt == null ? null : Timestamp.valueOf(ldt);
    }

    @FunctionalInterface
    public interface SqlRunnable {
        void run() throws SQLException;
    }

    // =============================
    // Internal DTO
    // =============================
    public record SessionEntity(
            int id,
            SessionStatus status,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            String feedback,
            int activeSeconds,
            LocalDateTime lastResumedAt
    ) {}
}