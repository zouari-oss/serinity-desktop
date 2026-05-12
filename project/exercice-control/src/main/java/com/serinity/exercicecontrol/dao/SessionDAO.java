package com.serinity.exercicecontrol.dao;

import com.serinity.exercicecontrol.config.DbConnection;

import com.serinity.exercicecontrol.service.SessionStatus;

import java.sql.*;
import java.time.LocalDateTime;

public class SessionDAO {
    private volatile Boolean hasActiveSeconds;
    private volatile Boolean hasLastResumedAt;
    private volatile String exerciseColumn;

    private boolean hasColumn(Connection cnx, String table, String column) throws SQLException {
        DatabaseMetaData metaData = cnx.getMetaData();
        try (ResultSet rs = metaData.getColumns(cnx.getCatalog(), null, table, column)) {
            return rs.next();
        }
    }

    private String exerciseColumn(Connection cnx) throws SQLException {
        String value = exerciseColumn;
        if (value != null) {
            return value;
        }
        synchronized (this) {
            if (exerciseColumn == null) {
                if (hasColumn(cnx, "exercise_session", "exercise_id")) {
                    exerciseColumn = "exercise_id";
                } else if (hasColumn(cnx, "exercise_session", "exercice_id")) {
                    exerciseColumn = "exercice_id";
                } else {
                    exerciseColumn = "exercise_id";
                }
            }
            return exerciseColumn;
        }
    }

    private boolean hasActiveSeconds(Connection cnx) throws SQLException {
        Boolean value = hasActiveSeconds;
        if (value != null) {
            return value;
        }
        synchronized (this) {
            if (hasActiveSeconds == null) {
                hasActiveSeconds = hasColumn(cnx, "exercise_session", "active_seconds");
            }
            return hasActiveSeconds;
        }
    }

    private boolean hasLastResumedAt(Connection cnx) throws SQLException {
        Boolean value = hasLastResumedAt;
        if (value != null) {
            return value;
        }
        synchronized (this) {
            if (hasLastResumedAt == null) {
                hasLastResumedAt = hasColumn(cnx, "exercise_session", "last_resumed_at");
            }
            return hasLastResumedAt;
        }
    }

    // =============================
    // CREATE (CREATED session)
    // =============================
    public int createCreatedSession(int userId, int exerciseId) throws SQLException {
        Connection cnx = DbConnection.getConnection();
        boolean withActiveSeconds = hasActiveSeconds(cnx);
        boolean withLastResumedAt = hasLastResumedAt(cnx);
        String exerciseColumn = exerciseColumn(cnx);
        String sql = withActiveSeconds && withLastResumedAt
                ? "INSERT INTO exercise_session " +
                    "(user_id, " + exerciseColumn + ", status, started_at, completed_at, feedback, active_seconds, last_resumed_at) " +
                    "VALUES (?, ?, 'CREATED', NULL, NULL, NULL, 0, NULL)"
                : "INSERT INTO exercise_session " +
                    "(user_id, " + exerciseColumn + ", status, started_at, completed_at, feedback) " +
                    "VALUES (?, ?, 'CREATED', NULL, NULL, NULL)";

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
        Connection cnx = DbConnection.getConnection();
        String activeSecondsExpr = hasActiveSeconds(cnx) ? "active_seconds" : "0";
        String lastResumedExpr = hasLastResumedAt(cnx) ? "last_resumed_at" : "started_at";
        String sql = """
            SELECT id, status, started_at, completed_at, feedback,
        """ + activeSecondsExpr + """
             AS active_seconds,
        """ + lastResumedExpr + """
             AS last_resumed_at
            FROM exercise_session
            WHERE id = ?
            FOR UPDATE
        """;

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
        Connection cnx = DbConnection.getConnection();
        boolean withActiveSeconds = hasActiveSeconds(cnx);
        boolean withLastResumedAt = hasLastResumedAt(cnx);
        StringBuilder sql = new StringBuilder("""
            UPDATE exercise_session
            SET status = ?,
                started_at = ?,
                completed_at = ?,
                feedback = ?
        """);
        if (withActiveSeconds) {
            sql.append(",\n                active_seconds = ?");
        }
        if (withLastResumedAt) {
            sql.append(",\n                last_resumed_at = ?");
        }
        sql.append("\n            WHERE id = ?\n");

        try (PreparedStatement ps = cnx.prepareStatement(sql.toString())) {
            int index = 1;
            ps.setString(index++, s.status().name());
            ps.setTimestamp(index++, toTs(s.startedAt()));
            ps.setTimestamp(index++, toTs(s.completedAt()));
            ps.setString(index++, s.feedback());
            if (withActiveSeconds) {
                ps.setInt(index++, s.activeSeconds());
            }
            if (withLastResumedAt) {
                ps.setTimestamp(index++, toTs(s.lastResumedAt()));
            }
            ps.setInt(index, s.id());
            ps.executeUpdate();
        }
    }

    // =============================
    // TRANSACTION WRAPPER
    // =============================
    public void withTransaction(SqlRunnable block) throws SQLException {
        Connection cnx = DbConnection.getConnection();
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
