package com.serinity.exercicecontrol.dao;

import com.serinity.exercicecontrol.model.ExerciseSession;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExerciseSessionDao {

    private final Connection cnx;

    public ExerciseSessionDao() {
        this.cnx = DbConnection.getInstance().getConnection();
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
}
