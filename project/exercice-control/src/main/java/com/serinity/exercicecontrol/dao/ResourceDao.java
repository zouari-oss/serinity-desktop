package com.serinity.exercicecontrol.dao;

import com.serinity.exercicecontrol.model.Resource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResourceDao {

    private final Connection cnx;


    private static final String TABLE = "exercice_resource";

    public ResourceDao() {
        this.cnx = DbConnection.getInstance().getConnection();
    }

    public int insert(Resource r) throws SQLException {
        String sql =
                "INSERT INTO " + TABLE + " (title, resource_type, resource_url, exercice_id) " +
                        "VALUES (?,?,?,?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getTitle());
            ps.setString(2, r.getMediaType());
            ps.setString(3, r.getUrl());
            ps.setInt(4, r.getExerciseId());
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
                        "SET title=?, resource_type=?, resource_url=?, exercice_id=? " +
                        "WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, r.getTitle());
            ps.setString(2, r.getMediaType());
            ps.setString(3, r.getUrl());
            ps.setInt(4, r.getExerciseId());
            ps.setInt(5, r.getId());
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
        String sql = "SELECT * FROM " + TABLE + " WHERE exercice_id=? ORDER BY id DESC";
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
        r.setMediaType(rs.getString("resource_type"));
        r.setUrl(rs.getString("resource_url"));
        r.setContent(null);
        r.setDurationSeconds(0);
        r.setExerciseId(rs.getInt("exercice_id"));
        return r;
    }
}
