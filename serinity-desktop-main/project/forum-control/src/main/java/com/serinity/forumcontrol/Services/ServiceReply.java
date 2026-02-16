package com.serinity.forumcontrol.Services;

import com.serinity.forumcontrol.Interfaces.Services;
import com.serinity.forumcontrol.Models.Reply;
import com.serinity.forumcontrol.Utils.MyDataBase;
import com.serinity.forumcontrol.HardcodedUser.FakeUser;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReply implements Services<Reply> {

    private Connection cnx;

    public ServiceReply() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Reply reply) {
        reply.setUserId(String.valueOf(FakeUser.getCurrentUserId()));

        String req = "INSERT INTO `replies` (`thread_id`, `user_id`, `parent_id`, `content`) VALUES (?, ?, ?, ?)";
        ServiceThread servicethread = new ServiceThread();
        servicethread.updateThreadCommentCount(reply.getThreadId(), 1);

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setLong(1, reply.getThreadId());
            pstm.setString(2, reply.getUserId());

            if (reply.getParentId() != null) {
                pstm.setLong(3, reply.getParentId());
            } else {
                pstm.setNull(3, Types.BIGINT);
            }

            pstm.setString(4, reply.getContent());

            pstm.executeUpdate();


        } catch (SQLException e) {
            System.out.println("Error adding reply: " + e.getMessage());
        }

    }

    @Override
    public List<Reply> getAll() {
        List<Reply> replies = new ArrayList<>();
        String req = "SELECT * FROM `replies` ORDER BY `created_at` ASC";

        try {
            Statement stm = this.cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);

            while (rs.next()) {
                Reply reply = mapResultSetToReply(rs);
                replies.add(reply);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving replies: " + e.getMessage());
        }

        return replies;
    }

    @Override
    public void update(Reply reply) {
        String req = "UPDATE `replies` SET `thread_id` = ?, `user_id` = ?, `parent_id` = ?, `content` = ? WHERE `id` = ?";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setLong(1, reply.getThreadId());
            pstm.setString(2, reply.getUserId());

            if (reply.getParentId() != null) {
                pstm.setLong(3, reply.getParentId());
            } else {
                pstm.setNull(3, Types.BIGINT);
            }

            pstm.setString(4, reply.getContent());
            pstm.setLong(5, reply.getId());

            int rowsAffected = pstm.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Reply updated successfully!");
            } else {
                System.out.println("No reply found with id: " + reply.getId());
            }

        } catch (SQLException e) {
            System.out.println("Error updating reply: " + e.getMessage());
        }
    }

    @Override
    public void delete(Reply reply) {
        String req = "DELETE FROM `replies` WHERE `id` = ?";
        ServiceThread servicethread = new ServiceThread();
        servicethread.updateThreadCommentCount(reply.getThreadId(), -1);
        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setLong(1, reply.getId());

            int rowsAffected = pstm.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Reply deleted successfully!");
            } else {
                System.out.println("No reply found with id: " + reply.getId());
            }

        } catch (SQLException e) {
            System.out.println("Error deleting reply: " + e.getMessage());
        }
    }

    /**
     * Get reply by ID
     */
    public Reply getById(long id) {
        String req = "SELECT * FROM `replies` WHERE `id` = ?";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setLong(1, id);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                return mapResultSetToReply(rs);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving reply by id: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get all replies for a specific thread
     */
    public List<Reply> getByThread(long threadId) {
        List<Reply> replies = new ArrayList<>();
        String req = "SELECT * FROM `replies` WHERE `thread_id` = ? ORDER BY `created_at` ASC";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setLong(1, threadId);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                Reply reply = mapResultSetToReply(rs);
                replies.add(reply);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving replies by thread: " + e.getMessage());
        }

        return replies;
    }

    /**
     * Get replies by user (String userId)
     */
    public List<Reply> getByUser(String userId) {
        List<Reply> replies = new ArrayList<>();
        String req = "SELECT * FROM `replies` WHERE `user_id` = ? ORDER BY `created_at` DESC";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setString(1, userId);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                Reply reply = mapResultSetToReply(rs);
                replies.add(reply);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving replies by user: " + e.getMessage());
        }

        return replies;
    }

    /**
     * Get replies by current user (using Sessiontest)
     */
    public List<Reply> getByCurrentUser() {
        return getByUser(String.valueOf(FakeUser.getCurrentUserId()));
    }

    /**
     * Count replies by user
     */
    public int countRepliesByUser(String userId) {
        String req = "SELECT COUNT(*) FROM `replies` WHERE `user_id` = ?";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setString(1, userId);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("Error counting replies by user: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Get all top-level replies for a thread (parent_id IS NULL)
     */
    public List<Reply> getTopLevelReplies(long threadId) {
        List<Reply> replies = new ArrayList<>();
        String req = "SELECT * FROM `replies` WHERE `thread_id` = ? AND `parent_id` IS NULL ORDER BY `created_at` ASC";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setLong(1, threadId);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                Reply reply = mapResultSetToReply(rs);
                replies.add(reply);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving top-level replies: " + e.getMessage());
        }

        return replies;
    }

    /**
     * Get all nested replies (replies to a specific reply)
     */
    public List<Reply> getNestedReplies(long parentId) {
        List<Reply> replies = new ArrayList<>();
        String req = "SELECT * FROM `replies` WHERE `parent_id` = ? ORDER BY `created_at` ASC";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setLong(1, parentId);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                Reply reply = mapResultSetToReply(rs);
                replies.add(reply);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving nested replies: " + e.getMessage());
        }

        return replies;
    }

    /**
     * Count total replies for a thread
     */
    public int countRepliesByThread(long threadId) {
        String req = "SELECT COUNT(*) FROM `replies` WHERE `thread_id` = ?";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setLong(1, threadId);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("Error counting replies: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Count nested replies for a specific reply
     */
    public int countNestedReplies(long parentId) {
        String req = "SELECT COUNT(*) FROM `replies` WHERE `parent_id` = ?";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setLong(1, parentId);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("Error counting nested replies: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Delete all replies for a thread (useful when deleting a thread)
     */
    public void deleteByThread(long threadId) {
        String req = "DELETE FROM `replies` WHERE `thread_id` = ?";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setLong(1, threadId);

            int rowsAffected = pstm.executeUpdate();
            System.out.println(rowsAffected + " replies deleted for thread " + threadId);

        } catch (SQLException e) {
            System.out.println("Error deleting replies by thread: " + e.getMessage());
        }
    }

    /**
     * Delete all nested replies (when deleting a parent reply)
     */
    public void deleteNestedReplies(long parentId) {
        String req = "DELETE FROM `replies` WHERE `parent_id` = ?";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setLong(1, parentId);

            int rowsAffected = pstm.executeUpdate();
            System.out.println(rowsAffected + " nested replies deleted");

        } catch (SQLException e) {
            System.out.println("Error deleting nested replies: " + e.getMessage());
        }
    }

    /**
     * Delete a reply and all its nested replies (cascade delete)
     */
    public void deleteWithNested(long replyId) {
        // First delete all nested replies
        deleteNestedReplies(replyId);

        // Then delete the reply itself
        Reply reply = getById(replyId);
        if (reply != null) {
            delete(reply);
        }
    }

    /**
     * Search replies by content
     */
    public List<Reply> searchByContent(String keyword) {
        List<Reply> replies = new ArrayList<>();
        String req = "SELECT * FROM `replies` WHERE `content` LIKE ? ORDER BY `created_at` DESC";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setString(1, "%" + keyword + "%");
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                Reply reply = mapResultSetToReply(rs);
                replies.add(reply);
            }

        } catch (SQLException e) {
            System.out.println("Error searching replies: " + e.getMessage());
        }

        return replies;
    }

    /**
     * Get recent replies (last N replies)
     */
    public List<Reply> getRecentReplies(int limit) {
        List<Reply> replies = new ArrayList<>();
        String req = "SELECT * FROM `replies` ORDER BY `created_at` DESC LIMIT ?";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setInt(1, limit);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                Reply reply = mapResultSetToReply(rs);
                replies.add(reply);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving recent replies: " + e.getMessage());
        }

        return replies;
    }

    /**
     * Update reply content only (most common update operation)
     */
    public void updateContent(long replyId, String newContent) {
        String req = "UPDATE `replies` SET `content` = ? WHERE `id` = ?";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setString(1, newContent);
            pstm.setLong(2, replyId);

            int rowsAffected = pstm.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Reply content updated successfully!");
            } else {
                System.out.println("No reply found with id: " + replyId);
            }

        } catch (SQLException e) {
            System.out.println("Error updating reply content: " + e.getMessage());
        }
    }

    /**
     * Check if user owns the reply
     */
    public boolean isOwner(long replyId, String userId) {
        Reply reply = getById(replyId);
        return reply != null && reply.getUserId().equals(userId);
    }

    /**
     * Check if current user owns the reply
     */
    public boolean isCurrentUserOwner(long replyId) {
        return isOwner(replyId, String.valueOf(FakeUser.getCurrentUserId()));
    }

    /**
     * Get the author (user_id) of a reply
     */
    public String getAuthor(long replyId) {
        Reply reply = getById(replyId);
        if (reply != null) {
            return reply.getUserId();
        }
        return null; // Return null if reply not found
    }

    /**
     * Get reply tree (parent with all its nested replies)
     * Returns a formatted string showing the hierarchy
     */
    public String getReplyTree(long parentId, int level) {
        StringBuilder tree = new StringBuilder();
        String indent = "  ".repeat(level);

        Reply parent = getById(parentId);
        if (parent != null) {
            tree.append(indent).append("└─ ").append(parent.getContent().substring(0, Math.min(50, parent.getContent().length()))).append("...\n");

            List<Reply> children = getNestedReplies(parentId);
            for (Reply child : children) {
                tree.append(getReplyTree(child.getId(), level + 1));
            }
        }

        return tree.toString();
    }
    public String getReplyAuthor(String userId) {

        String sql =
                "SELECT username FROM user WHERE user_id = ?";

        try (PreparedStatement ps =
                     cnx.prepareStatement(sql)) {

            ps.setString(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("username");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "User#" + userId;
    }

    /**
     * Helper method to map ResultSet to Reply object
     */
    private Reply mapResultSetToReply(ResultSet rs) throws SQLException {
        Reply reply = new Reply();
        reply.setId(rs.getLong("id"));
        reply.setThreadId(rs.getLong("thread_id"));
        reply.setUserId(rs.getString("user_id"));

        long parentId = rs.getLong("parent_id");
        if (!rs.wasNull()) {
            reply.setParentId(parentId);
        } else {
            reply.setParentId(null);
        }

        reply.setContent(rs.getString("content"));
        reply.setCreatedAt(rs.getTimestamp("created_at"));
        reply.setUpdatedAt(rs.getTimestamp("updated_at"));

        return reply;
    }

}