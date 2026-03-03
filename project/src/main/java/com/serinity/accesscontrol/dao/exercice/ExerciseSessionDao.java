package com.serinity.accesscontrol.dao.exercice;

import com.serinity.accesscontrol.util.MyDataBase;

import com.serinity.accesscontrol.model.exercice.ExerciseSession;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExerciseSessionDao {

    private final Connection cnx;

    public ExerciseSessionDao() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

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

    public List<SessionSummary> findRecent(int userId, Integer exerciseId, int days, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT id, user_id, exercise_id, status, started_at, completed_at, " +
            "TIMESTAMPDIFF(SECOND, started_at, COALESCE(completed_at, NOW())) AS active_seconds, " +
            "feedback FROM exercise_session WHERE user_id=?");
        if (exerciseId != null) sql.append(" AND exercise_id=?");
        if (days > 0) sql.append(" AND started_at >= DATE_SUB(NOW(), INTERVAL ? DAY)");
        sql.append(" ORDER BY started_at DESC");
        if (limit > 0) sql.append(" LIMIT ?");

        List<SessionSummary> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setInt(idx++, userId);
            if (exerciseId != null) ps.setInt(idx++, exerciseId);
            if (days > 0) ps.setInt(idx++, days);
            if (limit > 0) ps.setInt(idx++, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp started = rs.getTimestamp("started_at");
                    Timestamp completed = rs.getTimestamp("completed_at");
                    list.add(new SessionSummary(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("exercise_id"),
                        rs.getString("status"),
                        started != null ? started.toLocalDateTime() : null,
                        completed != null ? completed.toLocalDateTime() : null,
                        rs.getInt("active_seconds"),
                        rs.getString("feedback")
                    ));
                }
            }
        }
        return list;
    }

    public record SessionSummary(
        int id, int userId, int exerciseId,
        String status,
        java.time.LocalDateTime startedAt,
        java.time.LocalDateTime completedAt,
        int activeSeconds,
        String feedback
    ) {
        public boolean completed() { return "completed".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status); }
        public java.time.LocalDate day() { return startedAt != null ? startedAt.toLocalDate() : java.time.LocalDate.now(); }
    }
}
