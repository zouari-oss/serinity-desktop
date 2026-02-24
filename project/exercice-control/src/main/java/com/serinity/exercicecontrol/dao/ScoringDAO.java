package com.serinity.exercicecontrol.dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ScoringDAO {

    public int countStarted(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM exercise_session WHERE user_id = ?";
        Connection cnx = DbConnection.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int countCompleted(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM exercise_session WHERE user_id = ? AND status = 'COMPLETED'";
        Connection cnx = DbConnection.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int sumActiveSecondsLast7Days(int userId) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(active_seconds), 0)
            FROM exercise_session
            WHERE user_id = ?
              AND status = 'COMPLETED'
              AND started_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
        """;
        Connection cnx = DbConnection.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public List<LocalDate> getCompletedDaysDesc(int userId) throws SQLException {
        String sql = """
            SELECT DISTINCT DATE(started_at) AS d
            FROM exercise_session
            WHERE user_id = ?
              AND status = 'COMPLETED'
              AND started_at IS NOT NULL
            ORDER BY d DESC
        """;
        Connection cnx = DbConnection.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<LocalDate> days = new ArrayList<>();
                while (rs.next()) {
                    days.add(rs.getDate("d").toLocalDate());
                }
                return days;
            }
        }
    }
}