package com.serinity.exercicecontrol.dao;

import com.serinity.exercicecontrol.model.Resource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResourceDao {

    private final Connection cnx;


    private static final String TABLE = "resource";

    public ResourceDao() {
        this.cnx = DbConnection.getInstance().getConnection();
    }

    public int insert(Resource r) throws SQLException {
        String sql =
                "INSERT INTO " + TABLE + " (title, media_type, url, content, duration_seconds, exercise_id) " +
                        "VALUES (?,?,?,?,?,?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getTitle());
            ps.setString(2, r.getMediaType());
            ps.setString(3, r.getUrl());
            ps.setString(4, r.getContent());
            ps.setInt(5, r.getDurationSeconds());
            ps.setInt(6, r.getExerciseId());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public void update(Resource r) throws SQLException {
        String sql =
                "UPDATE " + TABLE + " " +
                        "SET title=?, media_type=?, url=?, content=?, duration_seconds=?, exercise_id=? " +
                        "WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, r.getTitle());
            ps.setString(2, r.getMediaType());
            ps.setString(3, r.getUrl());
            ps.setString(4, r.getContent());
            ps.setInt(5, r.getDurationSeconds());
            ps.setInt(6, r.getExerciseId());
            ps.setInt(7, r.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM " + TABLE + " WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Resource findById(int id) throws SQLException {
        String sql = "SELECT * FROM " + TABLE + " WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Resource> findAll() throws SQLException {
        String sql = "SELECT * FROM " + TABLE + " ORDER BY id DESC";
        List<Resource> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Resource> findByExerciseId(int exerciseId) throws SQLException {
        String sql = "SELECT * FROM " + TABLE + " WHERE exercise_id=? ORDER BY id DESC";
        List<Resource> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, exerciseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private Resource map(ResultSet rs) throws SQLException {
        Resource r = new Resource();
        r.setId(rs.getInt("id"));
        r.setTitle(rs.getString("title"));
        r.setMediaType(rs.getString("media_type"));
        r.setUrl(rs.getString("url"));
        r.setContent(rs.getString("content"));
        r.setDurationSeconds(rs.getInt("duration_seconds"));
        r.setExerciseId(rs.getInt("exercise_id"));
        return r;
    }
}
