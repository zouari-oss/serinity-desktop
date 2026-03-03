package com.serinity.exercicecontrol.dao;

import com.serinity.exercicecontrol.model.ExerciseSession;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExerciseSessionDao {

    private final Connection cnx;

    public ExerciseSessionDao() {
        this.cnx = DbConnection.getInstance().getConnection();
    }

    // ===================== EXISTANT (NE PAS CASSER) =====================

    public int insert(ExerciseSession s) throws SQLException {
        String sql = "INSERT INTO exercise_session (user_id, exercise_id, status, started_at, completed_at, feedback) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getUserId());
            ps.setInt(2, s.getExerciseId());
            ps.setString(3, s.getStatus());
            ps.setTimestamp(4, s.getStartedAt() != null ? Timestamp.valueOf(s.getStartedAt()) : null);
            ps.setTimestamp(5, s.getCompletedAt() != null ? Timestamp.valueOf(s.getCompletedAt()) : null);
            ps.setString(6, s.getFeedback());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public void update(ExerciseSession s) throws SQLException {
        String sql = "UPDATE exercise_session SET user_id=?, exercise_id=?, status=?, started_at=?, completed_at=?, feedback=? " +
                "WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, s.getUserId());
            ps.setInt(2, s.getExerciseId());
            ps.setString(3, s.getStatus());
            ps.setTimestamp(4, s.getStartedAt() != null ? Timestamp.valueOf(s.getStartedAt()) : null);
            ps.setTimestamp(5, s.getCompletedAt() != null ? Timestamp.valueOf(s.getCompletedAt()) : null);
            ps.setString(6, s.getFeedback());
            ps.setInt(7, s.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM exercise_session WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public ExerciseSession findById(int id) throws SQLException {
        String sql = "SELECT * FROM exercise_session WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ✅ REQUIS par BehaviorChangeService et ExerciseSessionService
    public List<ExerciseSession> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM exercise_session WHERE user_id=? ORDER BY started_at DESC";
        List<ExerciseSession> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<ExerciseSession> findByUserAndExercise(int userId, int exerciseId) throws SQLException {
        String sql = "SELECT * FROM exercise_session WHERE user_id=? AND exercise_id=? ORDER BY started_at DESC";
        List<ExerciseSession> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, exerciseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    private ExerciseSession mapRow(ResultSet rs) throws SQLException {
        ExerciseSession s = new ExerciseSession();
        s.setId(rs.getInt("id"));
        s.setUserId(rs.getInt("user_id"));
        s.setExerciseId(rs.getInt("exercise_id"));
        s.setStatus(rs.getString("status"));

        Timestamp started = rs.getTimestamp("started_at");
        Timestamp completed = rs.getTimestamp("completed_at");
        s.setStartedAt(started != null ? started.toLocalDateTime() : null);
        s.setCompletedAt(completed != null ? completed.toLocalDateTime() : null);

        s.setFeedback(rs.getString("feedback"));
        return s;
    }

    // ===================== AJOUT POUR LE COACH (SANS CASSER) =====================

    /** Petit résumé orienté analytics/coach (ne touche pas ton model ExerciseSession). */
    public record SessionSummary(
            int id,
            int userId,
            int exerciseId,
            String status,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            int activeSeconds,
            String feedback
    ) {}

    /**
     * Sessions récentes (fenêtre days) pour un user et éventuellement un exercice.
     * NOTE: Si ta table n'a PAS "active_seconds", on calcule via TIMESTAMPDIFF.
     */
    public List<SessionSummary> findRecent(int userId, Integer exerciseId, int days, int limit) throws SQLException {
        String sql = """
            SELECT
              id, user_id, exercise_id, status, started_at, completed_at,
              COALESCE(
                /* si la colonne existe, MySQL la prendra, sinon on calcule avec TIMESTAMPDIFF */
                CASE
                  WHEN started_at IS NOT NULL AND completed_at IS NOT NULL
                  THEN TIMESTAMPDIFF(SECOND, started_at, completed_at)
                  ELSE 0
                END
              ) AS active_seconds_calc,
              feedback
            FROM exercise_session
            WHERE user_id = ?
              AND ( ? IS NULL OR exercise_id = ? )
              AND ( started_at IS NULL OR started_at >= (NOW() - INTERVAL ? DAY) )
            ORDER BY COALESCE(started_at, completed_at) DESC
            LIMIT ?
        """;

        List<SessionSummary> out = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);

            if (exerciseId == null) {
                ps.setNull(2, Types.INTEGER);
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(2, exerciseId);
                ps.setInt(3, exerciseId);
            }

            ps.setInt(4, Math.max(1, days));
            ps.setInt(5, Math.max(1, limit));

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

    private static LocalDateTime toLdt(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
    // ======== AJOUTS ADMIN / ANALYTICS (sans table user) ========

    /** Stats globales sur une fenêtre, pour n'importe quel user (tous users). */
    public record CompletionStats(int total, int completed, int completionRatePercent, int avgActiveSeconds) {}

    /** Sessions groupées par jour. */
    public record DaySummary(String day, int sessions, int completed) {}

    /** Top exercices sur une fenêtre. */
    public record TopExercise(int exerciseId, String exerciseTitle, int sessions, int completed) {}

    public List<SessionSummary> findRecentAnyUser(Integer exerciseId, int days, int limit) throws SQLException {
        String sql = """
        SELECT
          id, user_id, exercise_id, status, started_at, completed_at,
          COALESCE(
            CASE
              WHEN started_at IS NOT NULL AND completed_at IS NOT NULL
              THEN TIMESTAMPDIFF(SECOND, started_at, completed_at)
              ELSE 0
            END
          ) AS active_seconds_calc,
          feedback
        FROM exercise_session
        WHERE ( ? IS NULL OR exercise_id = ? )
          AND ( started_at IS NULL OR started_at >= (NOW() - INTERVAL ? DAY) )
        ORDER BY COALESCE(started_at, completed_at) DESC
        LIMIT ?
    """;

        List<SessionSummary> out = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            if (exerciseId == null) {
                ps.setNull(1, Types.INTEGER);
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(1, exerciseId);
                ps.setInt(2, exerciseId);
            }

            ps.setInt(3, Math.max(1, days));
            ps.setInt(4, Math.max(1, limit));

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

    /** Completion rate + avg activeSeconds, tous users. */
    public CompletionStats completionStatsAnyUser(Integer exerciseId, int days) throws SQLException {
        String sql = """
        SELECT
          COUNT(*) AS total,
          SUM(CASE WHEN status IS NOT NULL AND UPPER(status)='COMPLETED' THEN 1 ELSE 0 END) AS completed,
          AVG(
            CASE
              WHEN started_at IS NOT NULL AND completed_at IS NOT NULL
              THEN TIMESTAMPDIFF(SECOND, started_at, completed_at)
              ELSE 0
            END
          ) AS avg_active
        FROM exercise_session
        WHERE ( ? IS NULL OR exercise_id = ? )
          AND ( started_at IS NULL OR started_at >= (NOW() - INTERVAL ? DAY) )
    """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            if (exerciseId == null) {
                ps.setNull(1, Types.INTEGER);
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(1, exerciseId);
                ps.setInt(2, exerciseId);
            }
            ps.setInt(3, Math.max(1, days));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int completed = rs.getInt("completed");
                    int rate = (total == 0) ? 0 : (completed * 100) / total;
                    int avg = (int) Math.round(rs.getDouble("avg_active"));
                    return new CompletionStats(total, completed, rate, Math.max(0, avg));
                }
            }
        }
        return new CompletionStats(0, 0, 0, 0);
    }

    /** Sessions par jour, tous users (optionnel: filtre exercice). */
    public List<DaySummary> sessionsPerDayAnyUser(Integer exerciseId, int days) throws SQLException {
        String sql = """
        SELECT
          DATE(COALESCE(started_at, completed_at)) AS day,
          COUNT(*) AS sessions,
          SUM(CASE WHEN status IS NOT NULL AND UPPER(status)='COMPLETED' THEN 1 ELSE 0 END) AS completed
        FROM exercise_session
        WHERE ( ? IS NULL OR exercise_id = ? )
          AND ( started_at IS NULL OR started_at >= (NOW() - INTERVAL ? DAY) )
        GROUP BY DATE(COALESCE(started_at, completed_at))
        ORDER BY day DESC
    """;

        List<DaySummary> out = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            if (exerciseId == null) {
                ps.setNull(1, Types.INTEGER);
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(1, exerciseId);
                ps.setInt(2, exerciseId);
            }
            ps.setInt(3, Math.max(1, days));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String day = rs.getDate("day") != null ? rs.getDate("day").toString() : "—";
                    out.add(new DaySummary(day, rs.getInt("sessions"), rs.getInt("completed")));
                }
            }
        }
        return out;
    }

    /** Top exercices (join exercise pour récupérer le titre). */
    public List<TopExercise> topExercises(int days, int limit) throws SQLException {
        String sql = """
        SELECT
          es.exercise_id AS ex_id,
          COALESCE(e.title, CONCAT('#', es.exercise_id)) AS title,
          COUNT(*) AS sessions,
          SUM(CASE WHEN es.status IS NOT NULL AND UPPER(es.status)='COMPLETED' THEN 1 ELSE 0 END) AS completed
        FROM exercise_session es
        LEFT JOIN exercise e ON e.id = es.exercise_id
        WHERE ( es.started_at IS NULL OR es.started_at >= (NOW() - INTERVAL ? DAY) )
        GROUP BY es.exercise_id, COALESCE(e.title, CONCAT('#', es.exercise_id))
        ORDER BY sessions DESC
        LIMIT ?
    """;

        List<TopExercise> out = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, days));
            ps.setInt(2, Math.max(1, limit));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new TopExercise(
                            rs.getInt("ex_id"),
                            rs.getString("title"),
                            rs.getInt("sessions"),
                            rs.getInt("completed")
                    ));
                }
            }
        }
        return out;
    }
}