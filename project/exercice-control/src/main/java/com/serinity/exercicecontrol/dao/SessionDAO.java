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
            INSERT INTO exercice_control
                (user_id, exercice_id, status, started_at, completed_at, feedback, active_seconds, created_at, updated_at)
            VALUES
                (?, ?, 'CREATED', NULL, NULL, NULL, 0, ?, ?)
        """;

        Connection cnx = DbConnection.getInstance().getConnection();

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            ps.setString(1, resolveExistingUserId(cnx, userId));
            ps.setInt(2, exerciseId);
            ps.setTimestamp(3, now);
            ps.setTimestamp(4, now);

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
    // READ
    // =============================
    public SessionEntity findByIdForUpdate(int sessionId) throws SQLException {
        String sql = """
            SELECT id, status, started_at, completed_at, feedback, active_seconds
            FROM exercice_control
            WHERE id = ?
            FOR UPDATE
        """;

        Connection cnx = DbConnection.getInstance().getConnection();

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                SessionStatus status = SessionStatus.fromDb(rs.getString("status"));
                LocalDateTime startedAt = toLdt(rs.getTimestamp("started_at"));
                LocalDateTime lastResumedAt = status == SessionStatus.IN_PROGRESS ? startedAt : null;

                return new SessionEntity(
                        rs.getInt("id"),
                        status,
                        startedAt,
                        toLdt(rs.getTimestamp("completed_at")),
                        rs.getString("feedback"),
                        rs.getInt("active_seconds"),
                        lastResumedAt
                );
            }
        }
    }

    // =============================
    // UPDATE
    // =============================
    public void update(SessionEntity s) throws SQLException {
        String sql = """
            UPDATE exercice_control
            SET status = ?,
                started_at = ?,
                completed_at = ?,
                feedback = ?,
                active_seconds = ?
            WHERE id = ?
        """;

        Connection cnx = DbConnection.getInstance().getConnection();

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, s.status().name());
            ps.setTimestamp(2, toTs(s.startedAt()));
            ps.setTimestamp(3, toTs(s.completedAt()));
            ps.setString(4, s.feedback());
            ps.setInt(5, s.activeSeconds());
            ps.setInt(6, s.id());
            ps.executeUpdate();
        }
    }


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


    private static LocalDateTime toLdt(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }

    private static Timestamp toTs(LocalDateTime ldt) {
        return ldt == null ? null : Timestamp.valueOf(ldt);
    }

    private static String resolveExistingUserId(Connection cnx, int requestedUserId) throws SQLException {
        String requested = String.valueOf(requestedUserId);

        String exactSql = "SELECT id FROM users WHERE id = ? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(exactSql)) {
            ps.setString(1, requested);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("id");
            }
        }

        String fallbackSql = "SELECT id FROM users ORDER BY created_at ASC LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(fallbackSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString("id");
        }

        throw new SQLException("No existing Symfony user found in users table for session start.");
    }

    @FunctionalInterface
    public interface SqlRunnable {
        void run() throws SQLException;
    }


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
