package com.serinity.forumcontrol.Services;

import com.serinity.forumcontrol.Interfaces.Services;
import com.serinity.forumcontrol.Models.Thread;
import com.serinity.forumcontrol.Models.ThreadType;
import com.serinity.forumcontrol.Models.ThreadStatus;
import com.serinity.forumcontrol.Utils.MyDataBase;
import com.serinity.forumcontrol.HardcodedUser.FakeUser;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceThread implements Services<Thread> {

    private Connection cnx;

    public ServiceThread() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Thread thread) {
        // Automatically set the current user ID (convert int to String)
        thread.setUserId(String.valueOf(FakeUser.getCurrentUserId()));

        String req = "INSERT INTO `threads` (`category_id`, `user_id`, `title`, `content`, `type`, `status`, `is_pinned`) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setLong(1, thread.getCategoryId());
            pstm.setString(2, thread.getUserId());
            pstm.setString(3, thread.getTitle());
            pstm.setString(4, thread.getContent());
            pstm.setString(5, thread.getType().getValue());
            pstm.setString(6, thread.getStatus().getValue());
            pstm.setBoolean(7, thread.isPinned());

            pstm.executeUpdate();
            System.out.println("Thread added successfully by user " + thread.getUserId());

        } catch (SQLException e) {
        }
    }

    @Override
    public List<Thread> getAll() {
        List<Thread> threads = new ArrayList<>();
        String req = "SELECT * FROM `threads` ORDER BY `is_pinned` DESC, `created_at` DESC";

        try {
            Statement stm = this.cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);

            while (rs.next()) {
                Thread thread = mapResultSetToThread(rs);
                threads.add(thread);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving threads: " + e.getMessage());
        }

        return threads;
    }

    @Override
    public void update(Thread thread) {
        String req = "UPDATE `threads` SET `category_id` = ?, `user_id` = ?, `title` = ?, `content` = ?, `type` = ?, `status` = ?, `is_pinned` = ? WHERE `id` = ?";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setLong(1, thread.getCategoryId());
            pstm.setString(2, thread.getUserId());
            pstm.setString(3, thread.getTitle());
            pstm.setString(4, thread.getContent());
            pstm.setString(5, thread.getType().getValue());
            pstm.setString(6, thread.getStatus().getValue());
            pstm.setBoolean(7, thread.isPinned());
            pstm.setLong(8, thread.getId());

            int rowsAffected = pstm.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Thread updated successfully!");
            } else {
                System.out.println("No thread found with id: " + thread.getId());
            }

        } catch (SQLException e) {
            System.out.println("Error updating thread: " + e.getMessage());
        }
    }

    @Override
    public void delete(Thread thread) {
        String req = "DELETE FROM `threads` WHERE `id` = ?";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setLong(1, thread.getId());

            int rowsAffected = pstm.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Thread deleted successfully!");
            } else {
                System.out.println("No thread found with id: " + thread.getId());
            }

        } catch (SQLException e) {
            System.out.println("Error deleting thread: " + e.getMessage());
        }
    }


    public Thread getById(long id) {
        String req = "SELECT * FROM `threads` WHERE `id` = ?";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setLong(1, id);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                return mapResultSetToThread(rs);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving thread by id: " + e.getMessage());
        }

        return null;
    }

    public List<Thread> getByUser(String userId) {
        List<Thread> threads = new ArrayList<>();
        String req = "SELECT * FROM `threads` WHERE `user_id` = ? ORDER BY `created_at` DESC";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setString(1, userId);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                Thread thread = mapResultSetToThread(rs);
                threads.add(thread);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving threads by user: " + e.getMessage());
        }

        return threads;
    }

    public void updateStatus(long threadId, ThreadStatus status) {
        String req = "UPDATE `threads` SET `status` = ? WHERE `id` = ?";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setString(1, status.getValue());
            pstm.setLong(2, threadId);

            int rowsAffected = pstm.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Thread status updated to: " + status.getValue());
            } else {
                System.out.println("No thread found with id: " + threadId);
            }

        } catch (SQLException e) {
            System.out.println("Error updating thread status: " + e.getMessage());
        }
    }

    public void togglePin(long threadId) {
        String req = "UPDATE `threads` SET `is_pinned` = NOT `is_pinned` WHERE `id` = ?";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setLong(1, threadId);
            pstm.executeUpdate();
            System.out.println("Thread pin status toggled!");

        } catch (SQLException e) {
            System.out.println("Error toggling pin: " + e.getMessage());
        }
    }

    public boolean isOwner(long threadId, String userId) {
        Thread thread = getById(threadId);
        return thread != null && thread.getUserId().equals(userId);
    }

    public String getAuthor(String userId) {

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
    public boolean isAdmin(String userId){
        String sql =
                "SELECT admin FROM user WHERE user_id = ?";

        try (PreparedStatement ps =
                     cnx.prepareStatement(sql)) {

            ps.setString(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                if(rs.getString("admin").equals("1")){return true;}
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
    public String getCategory(long CategoryId) {

        String sql =
                "SELECT slug FROM categories WHERE id = ?";

        try (PreparedStatement ps =
                     cnx.prepareStatement(sql)) {

            ps.setLong(1, CategoryId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("slug");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "unknown";
    }
    public void updateThreadCommentCount(long threadId, int change) {
        String sql = "UPDATE `threads` SET `commentcount` = `commentcount` + ? WHERE `id` = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setInt(1, change);
            stmt.setLong(2, threadId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating comment count: " + e.getMessage());
        }
    }

    public void updateThreadVoteCounts(int threadId, int oldVote, int newVote) {
        String sql = "UPDATE `threads` SET " +
                "`likecount` = `likecount` + ?, " +
                "`dislikecount` = `dislikecount` + ? " +
                "WHERE `id` = ?";

        try {
            int likeChange = 0;
            int dislikeChange = 0;

            if (oldVote == 1 && newVote != 1) {
                likeChange = -1;
            } else if (oldVote != 1 && newVote == 1) {
                likeChange = 1;
            }

            if (oldVote == -1 && newVote != -1) {
                dislikeChange = -1;
            } else if (oldVote != -1 && newVote == -1) {
                dislikeChange = 1;
            }

            if (likeChange != 0 || dislikeChange != 0) {
                PreparedStatement stmt = cnx.prepareStatement(sql);
                stmt.setInt(1, likeChange);
                stmt.setInt(2, dislikeChange);
                stmt.setInt(3, threadId);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Thread counts updated: likecount " +
                            (likeChange >= 0 ? "+" : "") + likeChange +
                            ", dislikecount " +
                            (dislikeChange >= 0 ? "+" : "") + dislikeChange);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error updating thread vote counts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateThreadFollowCount(int threadId, boolean wasFollowing, boolean isFollowing) {
        String sql = "UPDATE `threads` SET `followcount` = `followcount` + ? WHERE `id` = ?";

        try {
            int followChange = 0;

            if (!wasFollowing && isFollowing) {
                followChange = 1;
            } else if (wasFollowing && !isFollowing) {
                followChange = -1;
            }

            // Only update if there's a change
            if (followChange != 0) {
                PreparedStatement stmt = cnx.prepareStatement(sql);
                stmt.setInt(1, followChange);
                stmt.setInt(2, threadId);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Thread followcount updated: " +
                            (followChange >= 0 ? "+" : "") + followChange);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error updating thread follow count: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private Thread mapResultSetToThread(ResultSet rs) throws SQLException {
        Thread thread = new Thread();
        thread.setId(rs.getLong("id"));
        thread.setCategoryId(rs.getLong("category_id"));
        thread.setUserId(rs.getString("user_id"));
        thread.setTitle(rs.getString("title"));
        thread.setContent(rs.getString("content"));
        thread.setType(ThreadType.fromString(rs.getString("type")));
        thread.setStatus(ThreadStatus.fromString(rs.getString("status")));
        thread.setPinned(rs.getBoolean("is_pinned"));
        thread.setCreatedAt(rs.getTimestamp("created_at"));
        thread.setUpdatedAt(rs.getTimestamp("updated_at"));
        thread.setLikecount(rs.getInt("likecount"));
        thread.setDislikecount(rs.getInt("dislikecount"));
        thread.setFollowcount(rs.getInt("followcount"));
        thread.setRepliescount(rs.getInt("repliescount"));

        return thread;
    }
}