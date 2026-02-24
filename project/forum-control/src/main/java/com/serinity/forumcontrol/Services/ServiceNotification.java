package com.serinity.forumcontrol.Services;

import com.serinity.forumcontrol.Models.Notification;
import com.serinity.forumcontrol.Models.Thread;
import com.serinity.forumcontrol.Utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing notifications
 */
public class ServiceNotification {

    private Connection cnx;
    private ServiceThread threadService;

    public ServiceNotification() {
        this.cnx = MyDataBase.getInstance().getCnx();
        this.threadService = new ServiceThread();
    }

    public void createNotification(Long threadId, String type, String actorUserId) {
        Thread thread = threadService.getById(threadId);
        if (thread == null) {
            return;
        }

        String threadOwner = thread.getUserId();

        if (threadOwner.equals(actorUserId)) {
            return;
        }


        String actorName = threadService.getAuthor(actorUserId);
        String threadTitle = thread.getTitle();

        if (threadTitle.length() > 50) {
            threadTitle = threadTitle.substring(0, 47) + "...";
        }

        String content = "";
        switch (type) {
            case "like":
                content = actorName + " liked your thread: \"" + threadTitle + "\"";
                break;
            case "dislike":
                content = actorName + " disliked your thread: \"" + threadTitle + "\"";
                break;
            case "follow":
                content = actorName + " followed your thread: \"" + threadTitle + "\"";
                break;
            case "comment":
                content = actorName + " commented on your thread: \"" + threadTitle + "\"";
                break;
            default:
                content = actorName + " interacted with your thread: \"" + threadTitle + "\"";
        }

        if (content.length() > 200) {
            content = content.substring(0, 197) + "...";
        }

        String sql = "INSERT INTO `notifications` (`thread_id`, `type`, `content`, `user_id`) " +
                "VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setLong(1, threadId);
            stmt.setString(2, type);
            stmt.setString(3, content);
            stmt.setString(4, threadOwner); // Notification goes to thread owner

            stmt.executeUpdate();
            System.out.println("Notification created: " + type + " for thread " + threadId);

        } catch (SQLException e) {
            System.err.println("Error creating notification: " + e.getMessage());
        }
    }

    /**
     * Get all notifications for a user (newest first)
     */
    public List<Notification> getUserNotifications(String userId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM `notifications` WHERE `user_id` = ? ORDER BY `date` DESC, `id` DESC";

        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting notifications: " + e.getMessage());
        }

        return notifications;
    }

    /**
     * Get unseen notifications for a user
     */
    public List<Notification> getUnseenNotifications(String userId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM `notifications` WHERE `user_id` = ? AND `seen` = 0 ORDER BY `date` DESC, `id` DESC";

        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting unseen notifications: " + e.getMessage());
        }

        return notifications;
    }

    /**
     * Count unseen notifications for a user
     */
    public int getUnseenCount(String userId) {
        String sql = "SELECT COUNT(*) as count FROM `notifications` WHERE `user_id` = ? AND `seen` = 0";

        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }

        } catch (SQLException e) {
            System.err.println("Error counting unseen notifications: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Mark all notifications as seen for a user
     */
    public void markAllAsSeen(String userId) {
        String sql = "UPDATE `notifications` SET `seen` = 1 WHERE `user_id` = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setString(1, userId);
            int rowsAffected = stmt.executeUpdate();

            System.out.println("Marked " + rowsAffected + " notifications as seen for user " + userId);

        } catch (SQLException e) {
            System.err.println("Error marking notifications as seen: " + e.getMessage());
        }
    }

    /**
     * Mark a specific notification as seen
     */
    public void markAsSeen(Long notificationId) {
        String sql = "UPDATE `notifications` SET `seen` = 1 WHERE `id` = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setLong(1, notificationId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error marking notification as seen: " + e.getMessage());
        }
    }

    /**
     * Delete a notification
     */
    public void deleteNotification(Long notificationId) {
        String sql = "DELETE FROM `notifications` WHERE `id` = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setLong(1, notificationId);
            stmt.executeUpdate();
            System.out.println("Notification deleted: " + notificationId);

        } catch (SQLException e) {
            System.err.println("Error deleting notification: " + e.getMessage());
        }
    }

    /**
     * Delete all notifications for a user
     */
    public void deleteAllForUser(String userId) {
        String sql = "DELETE FROM `notifications` WHERE `user_id` = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setString(1, userId);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Deleted " + rowsAffected + " notifications for user " + userId);

        } catch (SQLException e) {
            System.err.println("Error deleting notifications: " + e.getMessage());
        }
    }

    /**
     * Map ResultSet to Notification object
     */
    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setId(rs.getLong("id"));
        notification.setThreadId(rs.getLong("thread_id"));
        notification.setType(rs.getString("type"));
        notification.setContent(rs.getString("content"));
        notification.setSeen(rs.getBoolean("seen"));
        notification.setDate(rs.getTimestamp("date"));
        notification.setUserId(rs.getString("user_id"));
        return notification;
    }
}