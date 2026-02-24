package com.serinity.exercicecontrol.service;

import com.serinity.exercicecontrol.dao.FavoriteDao;
import com.serinity.exercicecontrol.model.Favorite;

import java.sql.SQLException;
import java.util.List;

public class FavoriteService {

    private final FavoriteDao favoriteDao;

    public FavoriteService() {
        this.favoriteDao = new FavoriteDao();
    }

    public boolean addFavorite(int userId, String favoriteType, int itemId) throws SQLException {
        validate(userId, favoriteType, itemId);

        if (favoriteDao.exists(userId, favoriteType.trim().toUpperCase(), itemId)) {
            return false;
        }

        Favorite f = new Favorite(userId, favoriteType.trim().toUpperCase(), itemId);
        favoriteDao.insert(f);
        return true;
    }

    public void removeFavorite(int userId, String favoriteType, int itemId) throws SQLException {
        validate(userId, favoriteType, itemId);
        favoriteDao.deleteByUserTypeAndItem(userId, favoriteType.trim().toUpperCase(), itemId);
    }

    public List<Favorite> getUserFavorites(int userId) throws SQLException {
        if (userId <= 0) throw new IllegalArgumentException("Invalid userId.");
        return favoriteDao.findByUserId(userId);
    }

    private void validate(int userId, String favoriteType, int itemId) {
        if (userId <= 0) throw new IllegalArgumentException("Invalid userId.");
        if (itemId <= 0) throw new IllegalArgumentException("Invalid itemId.");
        if (favoriteType == null || favoriteType.trim().isEmpty())
            throw new IllegalArgumentException("favoriteType is required.");

        String ft = favoriteType.trim().toUpperCase();
        if (!ft.equals("EXERCISE") && !ft.equals("RESOURCE")) {
            throw new IllegalArgumentException("favoriteType must be EXERCISE or RESOURCE.");
        }
    }
}
