package com.serinity.exercicecontrol.dao;

import com.serinity.exercicecontrol.config.DbConnection;

import com.serinity.exercicecontrol.model.Resource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResourceDao {

    private static final String TABLE = "resource";
    private volatile String exerciseColumn;

    private Connection connection() throws SQLException {
        return DbConnection.getConnection();
    }

    private String exerciseColumn() throws SQLException {
        String column = exerciseColumn;
        if (column != null) {
            return column;
        }

        synchronized (this) {
            column = exerciseColumn;
            if (column == null) {
                Connection cnx = connection();
                if (hasColumn(cnx, TABLE, "exercise_id")) {
                    column = "exercise_id";
                } else if (hasColumn(cnx, TABLE, "exercice_id")) {
                    column = "exercice_id";
                } else {
                    column = "exercise_id";
                }
                exerciseColumn = column;
            }
        }

        return column;
    }

    private boolean hasColumn(Connection cnx, String table, String column) throws SQLException {
        DatabaseMetaData metaData = cnx.getMetaData();
        try (ResultSet rs = metaData.getColumns(cnx.getCatalog(), null, table, column)) {
            return rs.next();
        }
    }

    public int insert(Resource r) throws SQLException {
        String exerciseColumn = exerciseColumn();
        String sql =
                "INSERT INTO " + TABLE + " (title, media_type, url, content, duration_seconds, " + exerciseColumn + ") " +
                        "VALUES (?,?,?,?,?,?)";

        try (PreparedStatement ps = connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
        String exerciseColumn = exerciseColumn();
        String sql =
                "UPDATE " + TABLE + " " +
                        "SET title=?, media_type=?, url=?, content=?, duration_seconds=?, " + exerciseColumn + "=? " +
                        "WHERE id=?";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
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
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Resource findById(int id) throws SQLException {
        String sql = "SELECT * FROM " + TABLE + " WHERE id=?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Resource> findAll() throws SQLException {
        String sql = "SELECT *, " + exerciseColumn() + " AS resource_exercise_id FROM " + TABLE + " ORDER BY id DESC";
        List<Resource> list = new ArrayList<>();
        try (PreparedStatement ps = connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Resource> findByExerciseId(int exerciseId) throws SQLException {
        String sql = "SELECT *, " + exerciseColumn() + " AS resource_exercise_id FROM " + TABLE +
                " WHERE " + exerciseColumn() + "=? ORDER BY id DESC";
        List<Resource> list = new ArrayList<>();
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
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
        r.setExerciseId(readExerciseId(rs));
        return r;
    }

    private int readExerciseId(ResultSet rs) throws SQLException {
        try {
            return rs.getInt("resource_exercise_id");
        } catch (SQLException ignored) {
        }

        try {
            return rs.getInt("exercise_id");
        } catch (SQLException ignored) {
        }

        return rs.getInt("exercice_id");
    }
}
