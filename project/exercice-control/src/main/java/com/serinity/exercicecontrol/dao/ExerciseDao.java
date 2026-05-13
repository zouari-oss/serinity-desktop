package com.serinity.exercicecontrol.dao;

import com.serinity.exercicecontrol.model.Exercise;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExerciseDao {

    private final Connection cnx;

    public ExerciseDao() {
        this.cnx = DbConnection.getInstance().getConnection();
    }

    public int insert(Exercise ex) throws SQLException {
        String sql =
                "INSERT INTO exercice(title, type, level, duration_minutes, description, benefits, tips, theme, guided_instructions, is_active, created_at, updated_at) " +
                        "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            ps.setString(1, ex.getTitle());
            ps.setString(2, ex.getType());
            ps.setInt(3, ex.getLevel());
            ps.setInt(4, ex.getDurationMinutes());
            ps.setString(5, ex.getDescription());
            ps.setString(6, ex.getBenefits());
            ps.setString(7, ex.getTips());
            ps.setString(8, ex.getTheme());
            ps.setString(9, ex.getGuidedInstructions());
            ps.setBoolean(10, ex.isActive());
            ps.setTimestamp(11, now);
            ps.setTimestamp(12, now);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public void update(Exercise ex) throws SQLException {
        String sql =
                "UPDATE exercice " +
                        "SET title=?, type=?, level=?, duration_minutes=?, description=?, benefits=?, tips=?, theme=?, guided_instructions=?, is_active=?, updated_at=? " +
                        "WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            ps.setString(1, ex.getTitle());
            ps.setString(2, ex.getType());
            ps.setInt(3, ex.getLevel());
            ps.setInt(4, ex.getDurationMinutes());
            ps.setString(5, ex.getDescription());
            ps.setString(6, ex.getBenefits());
            ps.setString(7, ex.getTips());
            ps.setString(8, ex.getTheme());
            ps.setString(9, ex.getGuidedInstructions());
            ps.setBoolean(10, ex.isActive());
            ps.setTimestamp(11, now);
            ps.setInt(12, ex.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM exercice WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Exercise findById(int id) throws SQLException {
        String sql = "SELECT * FROM exercice WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Exercise> findAll() throws SQLException {
        String sql = "SELECT * FROM exercice ORDER BY id DESC";
        List<Exercise> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Exercise> findByType(String type) throws SQLException {
        String sql = "SELECT * FROM exercice WHERE type=? ORDER BY id DESC";
        List<Exercise> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Exercise> findByLevel(int level) throws SQLException {
        String sql = "SELECT * FROM exercice WHERE level=? ORDER BY id DESC";
        List<Exercise> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, level);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private Exercise map(ResultSet rs) throws SQLException {
        Exercise ex = new Exercise();
        ex.setId(rs.getInt("id"));
        ex.setTitle(rs.getString("title"));
        ex.setType(rs.getString("type"));
        ex.setLevel(rs.getInt("level"));
        ex.setDurationMinutes(rs.getInt("duration_minutes"));
        ex.setDescription(rs.getString("description"));
        ex.setBenefits(rs.getString("benefits"));
        ex.setTips(rs.getString("tips"));
        ex.setTheme(rs.getString("theme"));
        ex.setGuidedInstructions(rs.getString("guided_instructions"));
        ex.setIsActive(rs.getBoolean("is_active"));
        return ex;
    }
}
