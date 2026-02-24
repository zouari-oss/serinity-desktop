package com.serinity.forumcontrol.Services;

import com.serinity.forumcontrol.Models.PostInteraction;
import com.serinity.forumcontrol.Models.Thread;
import com.serinity.forumcontrol.Models.ThreadStatus;
import com.serinity.forumcontrol.Models.ThreadType;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServicePostInteractionTest {

    private ServicePostInteraction service;
    private ServiceThread serviceThread;

    private int testThreadId;
    private final String testUserId = "1"; // must exist in user table

    @BeforeAll
    void setup() {
        service = new ServicePostInteraction();
        serviceThread = new ServiceThread();

        // Create a thread for interaction tests
        Thread thread = new Thread();
        thread.setCategoryId(1L); // make sure category 1 exists
        thread.setTitle("Interaction Test Thread");
        thread.setContent("Testing interactions...");
        thread.setType(ThreadType.DISCUSSION);
        thread.setStatus(ThreadStatus.OPEN);
        thread.setPinned(false);

        serviceThread.add(thread);

        Thread saved = serviceThread.getAll().stream()
                .filter(t -> t.getTitle().equals("Interaction Test Thread"))
                .findFirst()
                .orElse(null);

        assertNotNull(saved);
        testThreadId = (int) saved.getId();

        System.out.println("=== Starting ServicePostInteraction Tests ===");
    }

    @AfterAll
    void cleanup() {
        service.deleteByThread(testThreadId);
        Thread thread = serviceThread.getById(testThreadId);
        if (thread != null) {
            serviceThread.delete(thread);
        }
        System.out.println("=== ServicePostInteraction Tests Completed ===");
    }

    // ================= VOTING =================

    @Test
    @Order(1)
    @DisplayName("Should upvote successfully")
    void testUpvote() {
        service.upvote(testThreadId, testUserId);

        assertEquals(1, service.getUserVote(testThreadId, testUserId));
        assertTrue(service.hasUpvoted(testThreadId, testUserId));

        System.out.println("✓ Upvote successful");
    }

    @Test
    @Order(2)
    @DisplayName("Should toggle upvote (remove)")
    void testToggleUpvoteRemove() {
        service.toggleUpvote(testThreadId, testUserId);

        assertEquals(0, service.getUserVote(testThreadId, testUserId));

        System.out.println("✓ Toggle upvote removed vote");
    }

    @Test
    @Order(3)
    @DisplayName("Should downvote successfully")
    void testDownvote() {
        service.downvote(testThreadId, testUserId);

        assertEquals(-1, service.getUserVote(testThreadId, testUserId));
        assertTrue(service.hasDownvoted(testThreadId, testUserId));

        System.out.println("✓ Downvote successful");
    }

    @Test
    @Order(4)
    @DisplayName("Should remove vote")
    void testRemoveVote() {
        service.removeVote(testThreadId, testUserId);

        assertEquals(0, service.getUserVote(testThreadId, testUserId));

        System.out.println("✓ Vote removed successfully");
    }

    @Test
    @Order(5)
    @DisplayName("Should switch from upvote to downvote")
    void testSwitchVote() {
        service.upvote(testThreadId, testUserId);
        service.downvote(testThreadId, testUserId);

        assertEquals(-1, service.getUserVote(testThreadId, testUserId));

        System.out.println("✓ Switched vote from upvote to downvote");
    }

    // ================= FOLLOW =================

    @Test
    @Order(6)
    @DisplayName("Should follow thread")
    void testFollow() {
        service.follow(testThreadId, testUserId);

        assertTrue(service.isFollowing(testThreadId, testUserId));

        System.out.println("✓ Follow successful");
    }

    @Test
    @Order(7)
    @DisplayName("Should unfollow thread")
    void testUnfollow() {
        service.unfollow(testThreadId, testUserId);

        assertFalse(service.isFollowing(testThreadId, testUserId));

        System.out.println("✓ Unfollow successful");
    }

    @Test
    @Order(8)
    @DisplayName("Should toggle follow")
    void testToggleFollow() {
        service.toggleFollow(testThreadId, testUserId);
        assertTrue(service.isFollowing(testThreadId, testUserId));

        service.toggleFollow(testThreadId, testUserId);
        assertFalse(service.isFollowing(testThreadId, testUserId));

        System.out.println("✓ Toggle follow works correctly");
    }

    // ================= AUTO DELETE =================

    @Test
    @Order(9)
    @DisplayName("Should auto delete empty interaction")
    void testAutoDeleteEmptyInteraction() {

        service.upvote(testThreadId, testUserId);
        service.removeVote(testThreadId, testUserId);

        PostInteraction interaction =
                service.getInteraction(testThreadId, testUserId);

        assertNull(interaction);

        System.out.println("✓ Auto delete works when interaction becomes empty");
    }

    // ================= DELETE =================

    @Test
    @Order(10)
    @DisplayName("Should delete interaction by thread and user")
    void testDeleteByThreadAndUser() {
        service.upvote(testThreadId, testUserId);

        service.delete(testThreadId, testUserId);

        assertNull(service.getInteraction(testThreadId, testUserId));

        System.out.println("✓ Delete by thread and user successful");
    }

    @Test
    @Order(11)
    @DisplayName("Should delete all interactions by thread")
    void testDeleteByThread() {

        service.upvote(testThreadId, "2");
        service.follow(testThreadId, "3");

        service.deleteByThread(testThreadId);

        assertNull(service.getInteraction(testThreadId, "2"));
        assertNull(service.getInteraction(testThreadId, "3"));

        System.out.println("✓ Delete by thread successful");
    }

    // ================= RECALCULATE =================

    @Test
    @Order(12)
    @DisplayName("Should recalculate thread counts")
    void testRecalculateCounts() {

        service.upvote(testThreadId, "4");
        service.follow(testThreadId, "4");

        service.recalculateThreadCounts(testThreadId);

        Thread thread = serviceThread.getById(testThreadId);

        assertTrue(thread.getLikecount() >= 1);
        assertTrue(thread.getFollowcount() >= 1);

        System.out.println("✓ Recalculate counts successful");
    }

    // ================= EDGE CASE =================

    @Test
    @Order(13)
    @DisplayName("Should handle non-existent interaction gracefully")
    void testNonExistentInteraction() {

        assertDoesNotThrow(() ->
                service.delete(999999, "999999")
        );

        System.out.println("✓ Handled non-existent interaction gracefully");
    }
}
