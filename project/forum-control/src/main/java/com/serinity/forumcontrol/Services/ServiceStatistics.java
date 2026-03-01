package com.serinity.forumcontrol.Services;

import com.serinity.forumcontrol.Models.ForumStatistics;
import com.serinity.forumcontrol.Utils.MyDataBase;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for gathering forum statistics (Admin only)
 */
public class ServiceStatistics {

    private Connection cnx;

    public ServiceStatistics() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    /**
     * Get complete forum statistics
     */
    public ForumStatistics getForumStatistics() {
        ForumStatistics stats = new ForumStatistics();

        // Get thread statistics
        stats.setTotalThreads(getTotalThreads());
        stats.setOpenThreads(getThreadsByStatus("open"));
        stats.setLockedThreads(getThreadsByStatus("locked"));
        stats.setArchivedThreads(getThreadsByStatus("archived"));
        stats.setDiscussionThreads(getThreadsByType("discussion"));
        stats.setQuestionThreads(getThreadsByType("question"));
        stats.setAnnouncementThreads(getThreadsByType("announcement"));

        // Get user statistics
        stats.setTotalUsers(getTotalUsers());
        stats.setActiveUsersToday(getActiveUsersToday());
        stats.setActiveUsersThisWeek(getActiveUsersThisWeek());

        // Get interaction statistics
        stats.setTotalReplies(getTotalReplies());
        stats.setTotalLikes(getTotalLikes());
        stats.setTotalDislikes(getTotalDislikes());
        stats.setTotalFollows(getTotalFollows());

        // Get category statistics
        stats.setTotalCategories(getTotalCategories());

        return stats;
    }

    /**
     * Get total number of threads
     */
    private int getTotalThreads() {
        String sql = "SELECT COUNT(*) as count FROM threads";
        return executeCountQuery(sql);
    }

    /**
     * Get threads by status
     */
    private int getThreadsByStatus(String status) {
        String sql = "SELECT COUNT(*) as count FROM threads WHERE status = ?";
        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error getting threads by status: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get threads by type
     */
    private int getThreadsByType(String type) {
        String sql = "SELECT COUNT(*) as count FROM threads WHERE type = ?";
        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error getting threads by type: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get total number of replies
     */
    private int getTotalReplies() {
        String sql = "SELECT COUNT(*) as count FROM replies";
        return executeCountQuery(sql);
    }

    /**
     * Get total number of users
     */
    private int getTotalUsers() {
        String sql = "SELECT COUNT(*) as count FROM profiles";
        return executeCountQuery(sql);
    }

    /**
     * Get active users today (users who posted or replied today)
     */
    private int getActiveUsersToday() {
        String sql = "SELECT COUNT(DISTINCT user_id) as count FROM (" +
                "SELECT user_id FROM threads WHERE DATE(created_at) = CURDATE() " +
                "UNION " +
                "SELECT user_id FROM replies WHERE DATE(created_at) = CURDATE()" +
                ") as active";
        return executeCountQuery(sql);
    }

    /**
     * Get active users this week
     */
    private int getActiveUsersThisWeek() {
        String sql = "SELECT COUNT(DISTINCT user_id) as count FROM (" +
                "SELECT user_id FROM threads WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                "UNION " +
                "SELECT user_id FROM replies WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)" +
                ") as active";
        return executeCountQuery(sql);
    }

    /**
     * Get total likes
     */
    private int getTotalLikes() {
        String sql = "SELECT COUNT(*) as count FROM postinteraction WHERE vote = 1";
        return executeCountQuery(sql);
    }

    /**
     * Get total dislikes
     */
    private int getTotalDislikes() {
        String sql = "SELECT COUNT(*) as count FROM postinteraction WHERE vote = -1";
        return executeCountQuery(sql);
    }

    /**
     * Get total follows
     */
    private int getTotalFollows() {
        String sql = "SELECT COUNT(*) as count FROM postinteraction WHERE follow = 1";
        return executeCountQuery(sql);
    }

    /**
     * Get total categories
     */
    private int getTotalCategories() {
        String sql = "SELECT COUNT(*) as count FROM categories";
        return executeCountQuery(sql);
    }

    /**
     * Get top 10 most active users (by thread + reply count)
     */
    public Map<String, Integer> getTopActiveUsers() {
        Map<String, Integer> topUsers = new HashMap<>();
        String sql = "SELECT p.username, " +
                "(SELECT COUNT(*) FROM threads WHERE user_id = p.user_id) + " +
                "(SELECT COUNT(*) FROM replies WHERE user_id = p.user_id) as activity_count " +
                "FROM profiles p " +
                "ORDER BY activity_count DESC " +
                "LIMIT 10";

        try {
            Statement stmt = cnx.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                topUsers.put(rs.getString("username"), rs.getInt("activity_count"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting top active users: " + e.getMessage());
        }

        return topUsers;
    }

    /**
     * Get most popular threads (by like count)
     */
    public Map<String, Integer> getTopThreadsByLikes() {
        Map<String, Integer> topThreads = new HashMap<>();
        String sql = "SELECT title, likecount FROM threads ORDER BY likecount DESC LIMIT 10";

        try {
            Statement stmt = cnx.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String title = rs.getString("title");
                if (title.length() > 40) {
                    title = title.substring(0, 37) + "...";
                }
                topThreads.put(title, rs.getInt("likecount"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting top threads: " + e.getMessage());
        }

        return topThreads;
    }

    /**
     * Get threads created per day for the last 30 days
     */
    public Map<String, Integer> getThreadsPerDay() {
        Map<String, Integer> threadsPerDay = new HashMap<>();
        String sql = "SELECT DATE(created_at) as date, COUNT(*) as count " +
                "FROM threads " +
                "WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                "GROUP BY DATE(created_at) " +
                "ORDER BY date DESC";

        try {
            Statement stmt = cnx.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                threadsPerDay.put(rs.getString("date"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting threads per day: " + e.getMessage());
        }

        return threadsPerDay;
    }

    /**
     * Helper method to execute simple count queries
     */
    private int executeCountQuery(String sql) {
        try {
            Statement stmt = cnx.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error executing count query: " + e.getMessage());
        }
        return 0;
    }
}