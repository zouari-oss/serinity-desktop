package com.serinity.exercicecontrol.dao;

import com.serinity.exercicecontrol.model.Favorite;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDao {

    private final Connection cnx;

    public FavoriteDao() {
        this.cnx = DbConnection.getInstance().getConnection();
    }

    public int insert(Favorite f) throws SQLException {
        String sql = "INSERT INTO favorite (user_id, favorite_type, item_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, f.getUserId());
            ps.setString(2, f.getFavoriteType());
            ps.setInt(3, f.getItemId());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM favorite WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void deleteByUserTypeAndItem(int userId, String favoriteType, int itemId) throws SQLException {
        String sql = "DELETE FROM favorite WHERE user_id=? AND favorite_type=? AND item_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, favoriteType);
            ps.setInt(3, itemId);
            ps.executeUpdate();
        }
    }

    public boolean exists(int userId, String favoriteType, int itemId) throws SQLException {
        String sql = "SELECT 1 FROM favorite WHERE user_id=? AND favorite_type=? AND item_id=? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, favoriteType);
            ps.setInt(3, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Favorite> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM favorite WHERE user_id=? ORDER BY id DESC";
        List<Favorite> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    private Favorite mapRow(ResultSet rs) throws SQLException {
        Favorite f = new Favorite();
        f.setId(rs.getInt("id"));
        f.setUserId(rs.getInt("user_id"));
        f.setFavoriteType(rs.getString("favorite_type"));
        f.setItemId(rs.getInt("item_id"));
        return f;
    }
}
