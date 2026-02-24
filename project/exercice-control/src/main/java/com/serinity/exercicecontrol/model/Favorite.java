package com.serinity.exercicecontrol.model;

public class Favorite {
    private int id;
    private int userId;
    private String favoriteType;
    private int itemId;

    public Favorite() {}

    public Favorite(int id, int userId, String favoriteType, int itemId) {
        this.id = id;
        this.userId = userId;
        this.favoriteType = favoriteType;
        this.itemId = itemId;
    }

    public Favorite(int userId, String favoriteType, int itemId) {
        this.userId = userId;
        this.favoriteType = favoriteType;
        this.itemId = itemId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFavoriteType() { return favoriteType; }
    public void setFavoriteType(String favoriteType) { this.favoriteType = favoriteType; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    @Override
    public String toString() {
        return "Favorite{" +
                "id=" + id +
                ", userId=" + userId +
                ", favoriteType='" + favoriteType + '\'' +
                ", itemId=" + itemId +
                '}';
    }
}
