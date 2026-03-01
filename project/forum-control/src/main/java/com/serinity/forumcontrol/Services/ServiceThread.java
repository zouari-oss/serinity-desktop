package com.serinity.forumcontrol.Services;
import com.serinity.forumcontrol.Interfaces.Services;
import com.serinity.forumcontrol.Models.Thread;
import com.serinity.forumcontrol.Models.ThreadType;
import com.serinity.forumcontrol.Models.ThreadStatus;
import com.serinity.forumcontrol.Utils.MyDataBase;

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
        thread.setUserId(String.valueOf(com.serinity.forumcontrol.CurrentUser.CurrentUser.getCurrentUserId()));

        String req = "INSERT INTO `threads` (`category_id`, `user_id`, `title`, `content`,`image_url`, `type`, `status`, `is_pinned`) VALUES (?, ?, ?, ?, ?, ?, ?,?)";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setLong(1, thread.getCategoryId());
            pstm.setString(2, thread.getUserId());
            pstm.setString(3, thread.getTitle());
            pstm.setString(4, thread.getContent());
            pstm.setString(5, thread.getImageUrl());
            pstm.setString(6, thread.getType().getValue());
            pstm.setString(7, thread.getStatus().getValue());
            pstm.setBoolean(8, thread.isPinned());


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

    public List<Thread> getAll(String currentUserId) {
        List<Thread> threads = new ArrayList<>();

        String req = """
    SELECT t.*,
        COALESCE(cat_score.category_interaction_score, 0) AS category_score,
        COALESCE(owner_score.owner_interaction_score, 0) AS owner_score,
        CASE WHEN (
            EXISTS (SELECT 1 FROM postinteraction pi0 WHERE pi0.thread_id = t.id AND pi0.user_id = ?)
            OR EXISTS (SELECT 1 FROM replies r0 WHERE r0.thread_id = t.id AND r0.user_id = ?)
        ) THEN 1 ELSE 0 END AS already_interacted

    FROM threads t

    LEFT JOIN (
        SELECT t2.category_id,
            SUM(
                ABS(pi.vote) +
                pi.follow +
                COALESCE(reply_count.cnt, 0)
            ) AS category_interaction_score
        FROM postinteraction pi
        JOIN threads t2 ON pi.thread_id = t2.id
        LEFT JOIN (
            SELECT thread_id, COUNT(*) AS cnt
            FROM replies
            WHERE user_id = ?
            GROUP BY thread_id
        ) reply_count ON reply_count.thread_id = pi.thread_id
        WHERE pi.user_id = ?
        -- Exclude interactions on the thread itself to avoid self-boosting
        AND pi.thread_id NOT IN (
            SELECT pi_exclude.thread_id FROM postinteraction pi_exclude WHERE pi_exclude.user_id = ?
        )
        GROUP BY t2.category_id
    ) cat_score ON cat_score.category_id = t.category_id

    LEFT JOIN (
        SELECT t3.user_id AS owner_id,
            SUM(
                ABS(pi2.vote) +
                pi2.follow +
                COALESCE(reply_count2.cnt, 0)
            ) AS owner_interaction_score
        FROM postinteraction pi2
        JOIN threads t3 ON pi2.thread_id = t3.id
        LEFT JOIN (
            SELECT thread_id, COUNT(*) AS cnt
            FROM replies
            WHERE user_id = ?
            GROUP BY thread_id
        ) reply_count2 ON reply_count2.thread_id = pi2.thread_id
        WHERE pi2.user_id = ?
        AND pi2.thread_id NOT IN (
            SELECT pi_exclude2.thread_id FROM postinteraction pi_exclude2 WHERE pi_exclude2.user_id = ?
        )
        GROUP BY t3.user_id
    ) owner_score ON owner_score.owner_id = t.user_id

    ORDER BY
        t.is_pinned DESC,
        already_interacted ASC,
        (COALESCE(cat_score.category_interaction_score, 0) + COALESCE(owner_score.owner_interaction_score, 0)) DESC
""";

        try {
            PreparedStatement stm = this.cnx.prepareStatement(req);
            stm.setString(1, currentUserId);  // already_interacted EXISTS postinteraction
            stm.setString(2, currentUserId);  // already_interacted EXISTS replies
            stm.setString(3, currentUserId);  // reply_count in cat_score
            stm.setString(4, currentUserId);  // pi.user_id in cat_score
            stm.setString(5, currentUserId);  // exclusion in cat_score
            stm.setString(6, currentUserId);  // reply_count2 in owner_score
            stm.setString(7, currentUserId);  // pi2.user_id in owner_score
            stm.setString(8, currentUserId);


            ResultSet rs = stm.executeQuery();
            while (rs.next()) {
                Thread thread = mapResultSetToThread(rs);
                threads.add(thread);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving threads: " + e.getMessage());
        }

        return threads;
    }

    public String getThreadBadge(long threadId) {
        String sql = """
            WITH
            base AS (
                SELECT
                    t.id,
                    t.user_id,
                    t.likecount,
                    t.dislikecount,
                    t.followcount,
                    t.repliescount,
                    DATEDIFF(NOW(), t.created_at)  AS days_old
                FROM threads t
                WHERE t.id = ?
            ),

            recent_likes AS (
                SELECT COUNT(*) AS likes_live
                FROM postinteraction pi
                WHERE pi.thread_id = ?
                  AND pi.vote      = 1
            ),

            author_rep AS (
                SELECT COALESCE(SUM(pi.follow), 0) AS nb_followers
                FROM postinteraction pi
                JOIN threads t ON t.id = pi.thread_id
                WHERE t.user_id = (SELECT user_id FROM base)
            ),

            scoring AS (
                SELECT
                    b.likecount,
                    b.dislikecount,
                    COALESCE(
                        b.dislikecount * 1.0
                        / NULLIF(b.likecount + b.dislikecount, 0),
                    0) AS controversy_ratio,
                    (
                        b.likecount         * 3.0
                      + b.repliescount      * 3.0
                      + b.followcount       * 2.0
                      - b.dislikecount      * 1.0
                      + COALESCE(a.nb_followers, 0) * 1.5
                      + COALESCE(r.likes_live,   0) * 5.0
                      + (1.0 / (b.days_old + 1))   * 10.0
                    ) AS raw_score
                FROM base b
                CROSS JOIN recent_likes r
                CROSS JOIN author_rep   a
            ),

            final_score AS (
                SELECT
                    raw_score * (1.0 - controversy_ratio * 0.3) AS score_final
                FROM scoring
            )

            SELECT
                CASE
                    WHEN score_final >= 200 THEN '🏆 Elite'
                    WHEN score_final >= 100 THEN '🔥 Hot'
                    WHEN score_final >= 50 THEN '⭐ Rising'
                    WHEN score_final >= 20  THEN '💬 Active'
                    ELSE                        '🆕 New'
                END AS badge,
                ROUND(score_final, 2) AS score_final
            FROM final_score
            """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, threadId);   // base CTE
            ps.setLong(2, threadId);   // recent_likes CTE
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("badge");
            }
        } catch (SQLException e) {
            System.err.println("Error computing thread badge: " + e.getMessage());
        }
        return "🆕 New";
    }
    @Override
    public void update(Thread thread) {
        String req = "UPDATE `threads` SET `category_id` = ?, `user_id` = ?, `title` = ?, `content` = ?,`content` = ?, `type` = ?, `status` = ?, `is_pinned` = ? WHERE `id` = ?";

        try {
            PreparedStatement pstm = this.cnx.prepareStatement(req);
            pstm.setLong(1, thread.getCategoryId());
            pstm.setString(2, thread.getUserId());
            pstm.setString(3, thread.getTitle());
            pstm.setString(4, thread.getContent());
            pstm.setString(5, thread.getImageUrl());
            pstm.setString(6, thread.getType().getValue());
            pstm.setString(7, thread.getStatus().getValue());
            pstm.setBoolean(8, thread.isPinned());
            pstm.setLong(9, thread.getId());

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
    public boolean isAdmin(String userId){
        String sql =
                "SELECT role FROM profiles WHERE user_id = ?";

        try (PreparedStatement ps =
                     cnx.prepareStatement(sql)) {

            ps.setString(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                if(rs.getString("role").equals("admin")){return true;}
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
        String sql = "UPDATE `threads` SET `repliescount` = `repliescount` + ? WHERE `id` = ?";

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
        thread.setImageUrl(rs.getString("image_url"));

        return thread;
    }
}
