package com.serinity.forumcontrol.Services;

import com.serinity.forumcontrol.Models.PostInteraction;
import com.serinity.forumcontrol.Utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePostInteraction {

    private Connection cnx;

    public ServicePostInteraction() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    public void upvote(int threadId, String userId) {
        setVote(threadId, userId, 1);
    }

    public void downvote(int threadId, String userId) {
        setVote(threadId, userId, -1);
    }

    public void removeVote(int threadId, String userId) {
        setVote(threadId, userId, 0);
    }

    private void setVote(int threadId, String userId, int vote) {
        PostInteraction interaction = getInteraction(threadId, userId);
        int oldVote = interaction != null ? interaction.getVote() : 0;

        if (interaction == null) {
            if (vote != 0) {
                interaction = new PostInteraction(threadId, userId, false, vote);
                add(interaction);
                ServiceThread servicethread = new ServiceThread();
                servicethread.updateThreadVoteCounts(threadId, oldVote, vote);
            }
        } else {
            interaction.setVote(vote);
            updateWithAutoDelete(interaction);
            ServiceThread servicethread = new ServiceThread();
            servicethread.updateThreadVoteCounts(threadId, oldVote, vote);
        }
    }

    public void toggleUpvote(int threadId, String userId) {
        PostInteraction interaction = getInteraction(threadId, userId);

        if (interaction == null) {
            upvote(threadId, userId);
        } else if (interaction.hasUpvoted()) {
            removeVote(threadId, userId);
        } else {
            upvote(threadId, userId);
        }
    }

    public void toggleDownvote(int threadId, String userId) {
        PostInteraction interaction = getInteraction(threadId, userId);

        if (interaction == null) {
            downvote(threadId, userId);
        } else if (interaction.hasDownvoted()) {
            removeVote(threadId, userId);
        } else {
            downvote(threadId, userId);
        }
    }

    public int getUserVote(int threadId, String userId) {
        PostInteraction interaction = getInteraction(threadId, userId);
        return interaction != null ? interaction.getVote() : 0;
    }

    public boolean hasUpvoted(int threadId, String userId) {
        return getUserVote(threadId, userId) == 1;
    }

    public boolean hasDownvoted(int threadId, String userId) {
        return getUserVote(threadId, userId) == -1;
    }

    public void follow(int threadId, String userId) {
        setFollow(threadId, userId, true);
    }

    public void unfollow(int threadId, String userId) {
        setFollow(threadId, userId, false);
    }

    private void setFollow(int threadId, String userId, boolean follow) {
        PostInteraction interaction = getInteraction(threadId, userId);
        boolean wasFollowing = interaction != null && interaction.isFollow();

        if (interaction == null) {
            if (follow) {
                interaction = new PostInteraction(threadId, userId, true, 0);
                add(interaction);
                ServiceThread servicethread = new ServiceThread();
                servicethread.updateThreadFollowCount(threadId, wasFollowing, follow);
            }
        } else {
            interaction.setFollow(follow);
            updateWithAutoDelete(interaction);
            ServiceThread servicethread = new ServiceThread();
            servicethread.updateThreadFollowCount(threadId, wasFollowing, follow);
        }
    }

    public void toggleFollow(int threadId, String userId) {
        PostInteraction interaction = getInteraction(threadId, userId);

        if (interaction == null || !interaction.isFollow()) {
            follow(threadId, userId);
        } else {
            unfollow(threadId, userId);
        }
    }

    public boolean isFollowing(int threadId, String userId) {
        PostInteraction interaction = getInteraction(threadId, userId);
        return interaction != null && interaction.isFollow();
    }


    public void recalculateThreadCounts(int threadId) {
        String sql = "UPDATE `threads` SET " +
                "`likecount` = (SELECT COUNT(*) FROM `postinteraction` WHERE `thread_id` = ? AND `vote` = 1), " +
                "`dislikecount` = (SELECT COUNT(*) FROM `postinteraction` WHERE `thread_id` = ? AND `vote` = -1), " +
                "`followcount` = (SELECT COUNT(*) FROM `postinteraction` WHERE `thread_id` = ? AND `follow` = 1) " +
                "WHERE `id` = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setInt(1, threadId);
            stmt.setInt(2, threadId);
            stmt.setInt(3, threadId);
            stmt.setInt(4, threadId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Thread counts recalculated for thread " + threadId);
            }

        } catch (SQLException e) {
            System.err.println("Error recalculating thread counts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void add(PostInteraction interaction) {
        String sql = "INSERT INTO `postinteraction` (`thread_id`, `user_id`, `follow`, `vote`) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement stmt = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, interaction.getThreadId());
            stmt.setString(2, interaction.getUserId());
            stmt.setBoolean(3, interaction.isFollow());
            stmt.setInt(4, interaction.getVote());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    interaction.setId(keys.getInt(1));
                }
                System.out.println("Interaction created: " + interaction);
            }

        } catch (SQLException e) {
            System.err.println("Error adding interaction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateWithAutoDelete(PostInteraction interaction) {
        if (interaction.isEmpty()) {
            delete(interaction.getId());
            System.out.println("Interaction deleted (empty): " + interaction);
        } else {
            update(interaction);
        }
    }

    public void update(PostInteraction interaction) {
        String sql = "UPDATE `postinteraction` SET `follow` = ?, `vote` = ? WHERE `id` = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setBoolean(1, interaction.isFollow());
            stmt.setInt(2, interaction.getVote());
            stmt.setInt(3, interaction.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Interaction updated: " + interaction);
            }

        } catch (SQLException e) {
            System.err.println("Error updating interaction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM `postinteraction` WHERE `id` = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Interaction deleted (ID: " + id + ")");
            }

        } catch (SQLException e) {
            System.err.println("Error deleting interaction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void delete(int threadId, String userId) {
        PostInteraction interaction = getInteraction(threadId, userId);

        if (interaction != null) {
            int oldVote = interaction.getVote();
            boolean wasFollowing = interaction.isFollow();

            String sql = "DELETE FROM `postinteraction` WHERE `thread_id` = ? AND `user_id` = ?";

            try {
                PreparedStatement stmt = cnx.prepareStatement(sql);
                stmt.setInt(1, threadId);
                stmt.setString(2, userId);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {

                    ServiceThread servicethread = new ServiceThread();
                    servicethread.updateThreadVoteCounts(threadId, oldVote, 0);
                    servicethread.updateThreadFollowCount(threadId, wasFollowing, false);

                    System.out.println("Interaction deleted (Thread: " + threadId + ", User: " + userId + ")");
                }

            } catch (SQLException e) {
                System.err.println("Error deleting interaction: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public PostInteraction getInteraction(int threadId, String userId) {
        String sql = "SELECT * FROM `postinteraction` WHERE `thread_id` = ? AND `user_id` = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setInt(1, threadId);
            stmt.setString(2, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToInteraction(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error getting interaction: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public PostInteraction getById(int id) {
        String sql = "SELECT * FROM `postinteraction` WHERE `id` = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToInteraction(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error getting interaction by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public List<PostInteraction> getByThread(int threadId) {
        List<PostInteraction> interactions = new ArrayList<>();
        String sql = "SELECT * FROM `postinteraction` WHERE `thread_id` = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setInt(1, threadId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                interactions.add(mapResultSetToInteraction(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting thread interactions: " + e.getMessage());
            e.printStackTrace();
        }

        return interactions;
    }

    public List<PostInteraction> getByUser(String userId) {
        List<PostInteraction> interactions = new ArrayList<>();
        String sql = "SELECT * FROM `postinteraction` WHERE `user_id` = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                interactions.add(mapResultSetToInteraction(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting user interactions: " + e.getMessage());
            e.printStackTrace();
        }

        return interactions;
    }

    public List<Integer> getFollowedThreadIds(String userId) {
        List<Integer> threadIds = new ArrayList<>();
        String sql = "SELECT `thread_id` FROM `postinteraction` WHERE `user_id` = ? AND `follow` = 1";

        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                threadIds.add(rs.getInt("thread_id"));
            }

        } catch (SQLException e) {
            System.err.println("Error getting followed threads: " + e.getMessage());
            e.printStackTrace();
        }

        return threadIds;
    }

    public VoteCounts getVoteCounts(int threadId) {
        String sql = "SELECT " +
                "SUM(CASE WHEN vote = 1 THEN 1 ELSE 0 END) as upvotes, " +
                "SUM(CASE WHEN vote = -1 THEN 1 ELSE 0 END) as downvotes " +
                "FROM `postinteraction` WHERE `thread_id` = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setInt(1, threadId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int upvotes = rs.getInt("upvotes");
                int downvotes = rs.getInt("downvotes");
                return new VoteCounts(upvotes, downvotes);
            }

        } catch (SQLException e) {
            System.err.println("Error getting vote counts: " + e.getMessage());
            e.printStackTrace();
        }

        return new VoteCounts(0, 0);
    }

    public int getFollowerCount(int threadId) {
        String sql = "SELECT COUNT(*) as count FROM `postinteraction` WHERE `thread_id` = ? AND `follow` = 1";

        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setInt(1, threadId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }

        } catch (SQLException e) {
            System.err.println("Error getting follower count: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    public void deleteByThread(int threadId) {
        String sql = "DELETE FROM `postinteraction` WHERE `thread_id` = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setInt(1, threadId);
            int rowsAffected = stmt.executeUpdate();

            System.out.println("Deleted " + rowsAffected + " interactions for thread " + threadId);

        } catch (SQLException e) {
            System.err.println("Error deleting thread interactions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private PostInteraction mapResultSetToInteraction(ResultSet rs) throws SQLException {
        PostInteraction interaction = new PostInteraction();
        interaction.setId(rs.getInt("id"));
        interaction.setThreadId(rs.getInt("thread_id"));
        interaction.setUserId(rs.getString("user_id"));
        interaction.setFollow(rs.getBoolean("follow"));
        interaction.setVote(rs.getInt("vote"));
        return interaction;
    }

    public static class VoteCounts {
        private int upvotes;
        private int downvotes;
        private int netVotes;

        public VoteCounts(int upvotes, int downvotes) {
            this.upvotes = upvotes;
            this.downvotes = downvotes;
            this.netVotes = upvotes - downvotes;
        }

        public int getUpvotes() { return upvotes; }
        public int getDownvotes() { return downvotes; }
        public int getNetVotes() { return netVotes; }

        @Override
        public String toString() {
            return "VoteCounts{upvotes=" + upvotes + ", downvotes=" + downvotes + ", net=" + netVotes + "}";
        }
    }
}