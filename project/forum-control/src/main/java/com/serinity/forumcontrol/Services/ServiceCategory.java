package com.serinity.forumcontrol.Services;

import com.serinity.forumcontrol.Interfaces.Services;
import com.serinity.forumcontrol.Models.Category;
import com.serinity.forumcontrol.Utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCategory implements Services<Category> {

    private Connection cnx;

    public ServiceCategory() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Category category) {
        Category existing = getBySlug(category.getSlug());
        if (existing != null ) {
            System.out.println("Error: Category with slug '" + category.getSlug() + "' already exists!");
            return;
        }
        String req = "INSERT INTO `categories` (`name`, `slug`, `description`, `parent_id`) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setString(1, category.getName());
            pstm.setString(2, category.getSlug());
            pstm.setString(3, category.getDescription());

            if (category.getParentId() != null) {
                pstm.setLong(4, category.getParentId());
            } else {
                pstm.setNull(4, Types.BIGINT);
            }

            pstm.executeUpdate();
            System.out.println("Category added successfully!");

        } catch (SQLException e) {
            System.out.println("Error adding category: " + e.getMessage());
        }
    }

    @Override
    public List<Category> getAll() {
        List<Category> categories = new ArrayList<>();
        String req = "SELECT * FROM `categories`";

        try {
            Statement stm = this.cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);

            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getLong("id"));
                category.setName(rs.getString("name"));
                category.setSlug(rs.getString("slug"));
                category.setDescription(rs.getString("description"));

                long parentId = rs.getLong("parent_id");
                if (!rs.wasNull()) {
                    category.setParentId(parentId);
                } else {
                    category.setParentId(null);
                }

                categories.add(category);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving categories: " + e.getMessage());
        }

        return categories;
    }

    @Override
    public void update(Category category) {
        Category existing = getBySlug(category.getSlug());
        if (existing != null && existing.getId() != category.getId()) {
            System.out.println("Error: Another category with slug '" + category.getSlug() + "' already exists!");
            return;
        }
        String req = "UPDATE `categories` SET `name` = ?, `slug` = ?, `description` = ?, `parent_id` = ? WHERE `id` = ?";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setString(1, category.getName());
            pstm.setString(2, category.getSlug());
            pstm.setString(3, category.getDescription());

            if (category.getParentId() != null) {
                pstm.setLong(4, category.getParentId());
            } else {
                pstm.setNull(4, Types.BIGINT);
            }

            pstm.setLong(5, category.getId());

            int rowsAffected = pstm.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Category updated successfully!");
            } else {
                System.out.println("No category found with id: " + category.getId());
            }

        } catch (SQLException e) {
            System.out.println("Error updating category: " + e.getMessage());
        }
    }

    @Override
    public void delete(Category category) {
        String req = "DELETE FROM `categories` WHERE `id` = ?";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setLong(1, category.getId());

            int rowsAffected = pstm.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Category deleted successfully!");
            } else {
                System.out.println("No category found with id: " + category.getId());
            }

        } catch (SQLException e) {
            System.out.println("Error deleting category: " + e.getMessage());
        }
    }


    public Category getById(long id) {
        String req = "SELECT * FROM `categories` WHERE `id` = ?";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setLong(1, id);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                Category category = new Category();
                category.setId(rs.getLong("id"));
                category.setName(rs.getString("name"));
                category.setSlug(rs.getString("slug"));
                category.setDescription(rs.getString("description"));

                long parentId = rs.getLong("parent_id");
                if (!rs.wasNull()) {
                    category.setParentId(parentId);
                }

                return category;
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving category by id: " + e.getMessage());
        }

        return null;
    }

    public Category getBySlug(String slug) {
        String req = "SELECT * FROM `categories` WHERE `slug` = ?";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setString(1, slug);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                Category category = new Category();
                category.setId(rs.getLong("id"));
                category.setName(rs.getString("name"));
                category.setSlug(rs.getString("slug"));
                category.setDescription(rs.getString("description"));

                long parentId = rs.getLong("parent_id");
                if (!rs.wasNull()) {
                    category.setParentId(parentId);
                }

                return category;
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving category by slug: " + e.getMessage());
        }

        return null;
    }

    public List<Category> getSubcategories(long parentId) {
        List<Category> subcategories = new ArrayList<>();
        String req = "SELECT * FROM `categories` WHERE `parent_id` = ?";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setLong(1, parentId);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getLong("id"));
                category.setName(rs.getString("name"));
                category.setSlug(rs.getString("slug"));
                category.setDescription(rs.getString("description"));
                category.setParentId(rs.getLong("parent_id"));

                subcategories.add(category);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving subcategories: " + e.getMessage());
        }

        return subcategories;
    }

    public List<Category> getRootCategories() {
        List<Category> rootCategories = new ArrayList<>();
        String req = "SELECT * FROM `categories` WHERE `parent_id` IS NULL";

        try {
            Statement stm = this.cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);

            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getLong("id"));
                category.setName(rs.getString("name"));
                category.setSlug(rs.getString("slug"));
                category.setDescription(rs.getString("description"));
                category.setParentId(null);

                rootCategories.add(category);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving root categories: " + e.getMessage());
        }

        return rootCategories;
    }
    public void deleteByParent(long parentId) {
        String req = "DELETE FROM `categories` WHERE `parent_id` = ?";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setLong(1, parentId);

            int rowsAffected = pstm.executeUpdate();
            System.out.println(rowsAffected + " subcategories deleted for category " + parentId);

        } catch (SQLException e) {
            System.out.println("Error deleting categories by parent: " + e.getMessage());
        }
    }
}