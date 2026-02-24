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
    private ServiceNotification notificationService = new ServiceNotification();

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

            notificationService.createNotification(reply.getThreadId(), "comment", reply.getUserId());

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

    public String getReplyAuthor(String userId) {

        String sql =
                "SELECT username FROM profiles WHERE user_id = ?";

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