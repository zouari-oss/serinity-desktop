package com.serinity.exercicecontrol.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RecommendationDAO {

    public int countStarted(int userId, int exerciseId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM exercise_session WHERE user_id=? AND exercise_id=?";
        Connection cnx = DbConnection.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, exerciseId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int countCompleted(int userId, int exerciseId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM exercise_session WHERE user_id=? AND exercise_id=? AND status='COMPLETED'";
        Connection cnx = DbConnection.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, exerciseId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int countAborted(int userId, int exerciseId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM exercise_session WHERE user_id=? AND exercise_id=? AND status='ABORTED'";
        Connection cnx = DbConnection.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, exerciseId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public boolean doneToday(int userId, int exerciseId) throws SQLException {
        String sql = """
            SELECT 1
            FROM exercise_session
            WHERE user_id=? AND exercise_id=?
              AND DATE(started_at) = CURDATE()
            LIMIT 1
        """;
        Connection cnx = DbConnection.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, exerciseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}