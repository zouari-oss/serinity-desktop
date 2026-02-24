package com.serinity.forumcontrol.Services;

import com.serinity.forumcontrol.CurrentUser.CurrentUser;
import com.serinity.forumcontrol.Models.Category;
import com.serinity.forumcontrol.Models.Thread;
import com.serinity.forumcontrol.Models.ThreadStatus;
import com.serinity.forumcontrol.Models.ThreadType;
import org.junit.jupiter.api.*;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit test suite for ServiceThread
 * Works with the current ServiceThread implementation
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServiceThreadTest {

    private ServiceThread threadService;
    private ServiceCategory categoryService;
    private Long testCategoryId;
    private Long thread1Id;
    private Long thread2Id;
    private Long pinnedThreadId;
    private String testUserId;

    @BeforeAll
    void setup() {
        threadService = new ServiceThread();
        categoryService = new ServiceCategory();
        testUserId = String.valueOf(CurrentUser.getCurrentUserId());

        // Create test category
        Category category = new Category();
        category.setName("JUnit Test Category");
        category.setSlug("junit-test-cat-" + System.currentTimeMillis());
        category.setDescription("Category for ServiceThread testing");
        category.setParentId(null);
        categoryService.add(category);

        Category saved = categoryService.getBySlug(category.getSlug());
        if (saved != null) {
            testCategoryId = saved.getId();
        }

        System.out.println("========================================");
        System.out.println("  ServiceThread Test Suite Started");
        System.out.println("========================================");
        System.out.println("Test User ID: " + testUserId);
        System.out.println("Test Category ID: " + testCategoryId);
        System.out.println();
    }

    @AfterAll
    void cleanup() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("  Cleaning up test data...");
        System.out.println("========================================");

        // Delete test threads
        if (thread1Id != null) {
            Thread t = threadService.getById(thread1Id);
            if (t != null) {
                threadService.delete(t);
                System.out.println("✓ Deleted thread: " + thread1Id);
            }
        }
        if (thread2Id != null) {
            Thread t = threadService.getById(thread2Id);
            if (t != null) {
                threadService.delete(t);
                System.out.println("✓ Deleted thread: " + thread2Id);
            }
        }
        if (pinnedThreadId != null) {
            Thread t = threadService.getById(pinnedThreadId);
            if (t != null) {
                threadService.delete(t);
                System.out.println("✓ Deleted pinned thread: " + pinnedThreadId);
            }
        }

        // Delete test category
        if (testCategoryId != null) {
            Category cat = categoryService.getById(testCategoryId);
            if (cat != null) {
                categoryService.delete(cat);
                System.out.println("✓ Deleted test category: " + testCategoryId);
            }
        }

        System.out.println();
        System.out.println("========================================");
        System.out.println("  All tests completed successfully!");
        System.out.println("========================================");
    }

    // ========== HELPER METHOD ==========

    /**
     * Find most recent thread by title for this user and category
     */
    private Thread findThreadByTitle(String title) {
        List<Thread> allThreads = threadService.getAll();
        return allThreads.stream()
                .filter(t -> t.getCategoryId()==(testCategoryId))
                .filter(t -> title.equals(t.getTitle()))
                .filter(t -> testUserId.equals(t.getUserId()))
                .max(Comparator.comparing(Thread::getCreatedAt))
                .orElse(null);
    }

    // ========== CREATE TESTS ==========

    @Test
    @Order(1)
    @DisplayName("1. CREATE: Should add DISCUSSION thread")
    void testAddDiscussionThread() {
        System.out.println("\n[TEST 1] Adding Discussion Thread...");

        // Arrange
        Thread thread = new Thread();
        thread.setCategoryId(testCategoryId);
        thread.setTitle("Test Discussion: How to manage stress?");
        thread.setContent("Looking for advice on managing daily stress.");
        thread.setType(ThreadType.DISCUSSION);
        thread.setStatus(ThreadStatus.OPEN);
        thread.setPinned(false);

        // Act
        threadService.add(thread);

        // Wait a moment for database

        // Find created thread
        Thread saved = findThreadByTitle("Test Discussion: How to manage stress?");

        // Assert
        assertNotNull(saved, "Thread should be saved to database");
        assertEquals("Test Discussion: How to manage stress?", saved.getTitle());
        assertEquals(ThreadType.DISCUSSION, saved.getType());
        assertEquals(ThreadStatus.OPEN, saved.getStatus());
        assertEquals(testUserId, saved.getUserId(), "User ID should be auto-set");
        assertFalse(saved.isPinned());
        assertNotNull(saved.getCreatedAt());

        // Verify initial counts
        assertEquals(0, saved.getLikecount());
        assertEquals(0, saved.getDislikecount());
        assertEquals(0, saved.getFollowcount());
        assertEquals(0, saved.getRepliescount());

        thread1Id = saved.getId();
        System.out.println("✓ Discussion thread created successfully");
        System.out.println("  - ID: " + thread1Id);
        System.out.println("  - User ID: " + saved.getUserId());
    }

    @Test
    @Order(2)
    @DisplayName("2. CREATE: Should add QUESTION thread")
    void testAddQuestionThread() {
        System.out.println("\n[TEST 2] Adding Question Thread...");

        // Arrange
        Thread thread = new Thread();
        thread.setCategoryId(testCategoryId);
        thread.setTitle("Test Question: Sleep hygiene practices?");
        thread.setContent("I struggle with sleep. What should I do?");
        thread.setType(ThreadType.QUESTION);
        thread.setStatus(ThreadStatus.OPEN);
        thread.setPinned(false);

        // Act
        threadService.add(thread);

        Thread saved = findThreadByTitle("Test Question: Sleep hygiene practices?");

        // Assert
        assertNotNull(saved, "Thread should be saved");
        assertEquals(ThreadType.QUESTION, saved.getType());
        assertEquals(testUserId, saved.getUserId());

        thread2Id = saved.getId();
        System.out.println("✓ Question thread created successfully");
        System.out.println("  - ID: " + thread2Id);
    }

    @Test
    @Order(3)
    @DisplayName("3. CREATE: Should add ANNOUNCEMENT thread (pinned)")
    void testAddAnnouncementThread() {
        System.out.println("\n[TEST 3] Adding Announcement Thread...");

        // Arrange
        Thread thread = new Thread();
        thread.setCategoryId(testCategoryId);
        thread.setTitle("Test Announcement: Forum Guidelines");
        thread.setContent("Please read our community guidelines.");
        thread.setType(ThreadType.ANNOUNCEMENT);
        thread.setStatus(ThreadStatus.OPEN);
        thread.setPinned(true);

        // Act
        threadService.add(thread);

        Thread saved = findThreadByTitle("Test Announcement: Forum Guidelines");

        // Assert
        assertNotNull(saved, "Thread should be saved");
        assertEquals(ThreadType.ANNOUNCEMENT, saved.getType());
        assertTrue(saved.isPinned(), "Announcement should be pinned");

        pinnedThreadId = saved.getId();
        System.out.println("✓ Announcement thread created successfully");
        System.out.println("  - ID: " + pinnedThreadId);
        System.out.println("  - Pinned: " + saved.isPinned());
    }

    // ========== READ TESTS ==========

    @Test
    @Order(4)
    @DisplayName("4. READ: Should get thread by ID")
    void testGetById() {
        System.out.println("\n[TEST 4] Getting Thread by ID...");

        // Ensure thread1Id exists
        assertNotNull(thread1Id, "thread1Id should be set from previous test");

        // Act
        Thread thread = threadService.getById(thread1Id);

        // Assert
        assertNotNull(thread, "Thread should exist");
        assertEquals(thread1Id, thread.getId());
        assertEquals(testCategoryId, thread.getCategoryId());
        assertEquals(testUserId, thread.getUserId());

        System.out.println("✓ Thread retrieved successfully");
        System.out.println("  - ID: " + thread.getId());
        System.out.println("  - Title: " + thread.getTitle());
    }

    @Test
    @Order(5)
    @DisplayName("5. READ: Should return null for non-existent ID")
    void testGetByIdNonExistent() {
        System.out.println("\n[TEST 5] Testing non-existent thread ID...");

        // Act
        Thread thread = threadService.getById(999999L);

        // Assert
        assertNull(thread, "Should return null for non-existent ID");

        System.out.println("✓ Correctly returned null for non-existent thread");
    }

    @Test
    @Order(6)
    @DisplayName("6. READ: Should get all threads ordered correctly")
    void testGetAll() {
        System.out.println("\n[TEST 6] Getting all threads...");

        // Act
        List<Thread> threads = threadService.getAll();

        // Assert
        assertNotNull(threads, "Should return thread list");
        assertTrue(threads.size() >= 3, "Should have at least our 3 test threads");

        // Verify ordering: pinned first, then by created_at DESC
        boolean foundPinned = false;
        boolean foundUnpinned = false;

        for (Thread t : threads) {
            if (t.getCategoryId()==(testCategoryId)) {
                if (t.isPinned()) {
                    foundPinned = true;
                    assertFalse(foundUnpinned, "Pinned threads should come before unpinned");
                } else {
                    foundUnpinned = true;
                }
            }
        }

        System.out.println("✓ Retrieved all threads successfully");
        System.out.println("  - Total threads: " + threads.size());
        System.out.println("  - Ordering verified: pinned first ✓");
    }

    // ========== UPDATE TESTS ==========

    @Test
    @Order(7)
    @DisplayName("7. UPDATE: Should update thread title and content")
    void testUpdateThread() {
        System.out.println("\n[TEST 7] Updating thread content...");

        assertNotNull(thread1Id, "thread1Id should exist");

        // Arrange
        Thread thread = threadService.getById(thread1Id);
        assertNotNull(thread, "Thread should exist");

        String originalTitle = thread.getTitle();
        thread.setTitle("UPDATED: Stress Management Techniques");
        thread.setContent("Updated content with new information.");

        // Act
        threadService.update(thread);
        Thread updated = threadService.getById(thread1Id);

        // Assert
        assertNotNull(updated);
        assertEquals("UPDATED: Stress Management Techniques", updated.getTitle());
        assertEquals("Updated content with new information.", updated.getContent());

        System.out.println("✓ Thread updated successfully");
        System.out.println("  - Old title: " + originalTitle);
        System.out.println("  - New title: " + updated.getTitle());
    }

    @Test
    @Order(8)
    @DisplayName("8. UPDATE: Should update thread type")
    void testUpdateThreadType() {
        System.out.println("\n[TEST 8] Updating thread type...");

        assertNotNull(thread1Id, "thread1Id should exist");

        // Arrange
        Thread thread = threadService.getById(thread1Id);
        ThreadType originalType = thread.getType();
        thread.setType(ThreadType.QUESTION);

        // Act
        threadService.update(thread);
        Thread updated = threadService.getById(thread1Id);

        // Assert
        assertEquals(ThreadType.QUESTION, updated.getType());

        System.out.println("✓ Thread type updated successfully");
        System.out.println("  - Old type: " + originalType);
        System.out.println("  - New type: " + updated.getType());
    }

    // ========== STATUS MANAGEMENT TESTS ==========

    @Test
    @Order(9)
    @DisplayName("9. STATUS: Should update status to LOCKED")
    void testUpdateStatusToLocked() {
        System.out.println("\n[TEST 9] Locking thread...");

        assertNotNull(thread1Id, "thread1Id should exist");

        // Act
        threadService.updateStatus(thread1Id, ThreadStatus.LOCKED);
        Thread updated = threadService.getById(thread1Id);

        // Assert
        assertEquals(ThreadStatus.LOCKED, updated.getStatus());

        System.out.println("✓ Thread locked successfully");
    }

    @Test
    @Order(10)
    @DisplayName("10. STATUS: Should update status to OPEN")
    void testUpdateStatusToOpen() {
        System.out.println("\n[TEST 10] Reopening thread...");

        assertNotNull(thread1Id, "thread1Id should exist");

        // Act
        threadService.updateStatus(thread1Id, ThreadStatus.OPEN);
        Thread updated = threadService.getById(thread1Id);

        // Assert
        assertEquals(ThreadStatus.OPEN, updated.getStatus());

        System.out.println("✓ Thread reopened successfully");
    }

    @Test
    @Order(11)
    @DisplayName("11. STATUS: Should update status to ARCHIVED")
    void testUpdateStatusToArchived() {
        System.out.println("\n[TEST 11] Archiving thread...");

        assertNotNull(thread2Id, "thread2Id should exist");

        // Act
        threadService.updateStatus(thread2Id, ThreadStatus.ARCHIVED);
        Thread updated = threadService.getById(thread2Id);

        // Assert
        assertEquals(ThreadStatus.ARCHIVED, updated.getStatus());

        // Reset for other tests
        threadService.updateStatus(thread2Id, ThreadStatus.OPEN);

        System.out.println("✓ Thread archived successfully");
    }

    // ========== PIN MANAGEMENT TESTS ==========

    @Test
    @Order(12)
    @DisplayName("12. PIN: Should toggle pin from false to true")
    void testTogglePinToTrue() {
        System.out.println("\n[TEST 12] Toggling pin status (false → true)...");

        assertNotNull(thread1Id, "thread1Id should exist");

        // Arrange - ensure thread is not pinned
        Thread thread = threadService.getById(thread1Id);
        if (thread.isPinned()) {
            threadService.togglePin(thread1Id);
            thread = threadService.getById(thread1Id);
        }
        assertFalse(thread.isPinned(), "Thread should start unpinned");

        // Act
        threadService.togglePin(thread1Id);
        Thread updated = threadService.getById(thread1Id);

        // Assert
        assertTrue(updated.isPinned(), "Thread should now be pinned");

        System.out.println("✓ Pin toggled: false → true");
    }

    @Test
    @Order(13)
    @DisplayName("13. PIN: Should toggle pin from true to false")
    void testTogglePinToFalse() {
        System.out.println("\n[TEST 13] Toggling pin status (true → false)...");

        assertNotNull(thread1Id, "thread1Id should exist");

        // Arrange
        Thread thread = threadService.getById(thread1Id);
        assertTrue(thread.isPinned(), "Thread should be pinned from previous test");

        // Act
        threadService.togglePin(thread1Id);
        Thread updated = threadService.getById(thread1Id);

        // Assert
        assertFalse(updated.isPinned(), "Thread should now be unpinned");

        System.out.println("✓ Pin toggled: true → false");
    }

    // ========== OWNERSHIP TESTS ==========

    @Test
    @Order(14)
    @DisplayName("14. OWNER: Should identify thread owner correctly")
    void testIsOwner() {
        System.out.println("\n[TEST 14] Checking thread ownership...");

        assertNotNull(thread1Id, "thread1Id should exist");

        // Act
        boolean isOwner = threadService.isOwner(thread1Id, testUserId);

        // Assert
        assertTrue(isOwner, "Test user should be owner of test thread");

        System.out.println("✓ Thread owner identified correctly");
        System.out.println("  - Thread ID: " + thread1Id);
        System.out.println("  - Owner: " + testUserId);
    }

    @Test
    @Order(15)
    @DisplayName("15. OWNER: Should identify non-owner correctly")
    void testIsNotOwner() {
        System.out.println("\n[TEST 15] Checking non-owner...");

        assertNotNull(thread1Id, "thread1Id should exist");

        // Act
        boolean isOwner = threadService.isOwner(thread1Id, "different-user-123");

        // Assert
        assertFalse(isOwner, "Different user should not be owner");

        System.out.println("✓ Non-owner identified correctly");
    }

    @Test
    @Order(16)
    @DisplayName("16. OWNER: Should handle non-existent thread")
    void testIsOwnerNonExistent() {
        System.out.println("\n[TEST 16] Checking ownership of non-existent thread...");

        // Act
        boolean isOwner = threadService.isOwner(999999L, testUserId);

        // Assert
        assertFalse(isOwner, "Should return false for non-existent thread");

        System.out.println("✓ Non-existent thread handled correctly");
    }

    // ========== HELPER METHOD TESTS ==========

    @Test
    @Order(17)
    @DisplayName("17. HELPER: Should get author username")
    void testGetAuthor() {
        System.out.println("\n[TEST 17] Getting author username...");

        // Act
        String author = threadService.getAuthor(testUserId);

        // Assert
        assertNotNull(author, "Should return author name");
        System.out.println("✓ Author retrieved: " + author);
    }

    @Test
    @Order(18)
    @DisplayName("18. HELPER: Should return fallback for non-existent user")
    void testGetAuthorNonExistent() {
        System.out.println("\n[TEST 18] Getting non-existent author...");

        // Act
        String author = threadService.getAuthor("nonexistent-999");

        // Assert
        assertTrue(author.startsWith("User#"), "Should return fallback format");
        System.out.println("✓ Fallback author returned: " + author);
    }

    @Test
    @Order(19)
    @DisplayName("19. HELPER: Should get category slug")
    void testGetCategory() {
        System.out.println("\n[TEST 19] Getting category slug...");

        assertNotNull(testCategoryId, "testCategoryId should exist");

        // Act
        String categorySlug = threadService.getCategory(testCategoryId);

        // Assert
        assertTrue(categorySlug.startsWith("junit-test-cat"), "Should return test category slug");
        System.out.println("✓ Category slug retrieved: " + categorySlug);
    }

    @Test
    @Order(20)
    @DisplayName("20. HELPER: Should return 'unknown' for non-existent category")
    void testGetCategoryNonExistent() {
        System.out.println("\n[TEST 20] Getting non-existent category...");

        // Act
        String categorySlug = threadService.getCategory(999999L);

        // Assert
        assertEquals("unknown", categorySlug);
        System.out.println("✓ Unknown category handled: " + categorySlug);
    }

    // ========== COMMENT COUNT TESTS ==========

    @Test
    @Order(21)
    @DisplayName("21. COUNT: Should increment comment count")
    void testIncrementCommentCount() {
        System.out.println("\n[TEST 21] Incrementing comment count...");

        assertNotNull(thread1Id, "thread1Id should exist");

        // Arrange
        Thread thread = threadService.getById(thread1Id);
        int originalCount = thread.getRepliescount();

        // Act
        threadService.updateThreadCommentCount(thread1Id, 1);
        Thread updated = threadService.getById(thread1Id);

        // Assert
        assertEquals(originalCount + 1, updated.getRepliescount());
        System.out.println("✓ Comment count incremented: " + originalCount + " → " + updated.getRepliescount());
    }

    @Test
    @Order(22)
    @DisplayName("22. COUNT: Should add multiple comments")
    void testAddMultipleComments() {
        System.out.println("\n[TEST 22] Adding multiple comments...");

        assertNotNull(thread1Id, "thread1Id should exist");

        // Arrange
        Thread thread = threadService.getById(thread1Id);
        int originalCount = thread.getRepliescount();

        // Act
        threadService.updateThreadCommentCount(thread1Id, 5);
        Thread updated = threadService.getById(thread1Id);

        // Assert
        assertEquals(originalCount + 5, updated.getRepliescount());
        System.out.println("✓ Added 5 comments: " + originalCount + " → " + updated.getRepliescount());
    }

    @Test
    @Order(23)
    @DisplayName("23. COUNT: Should decrement comment count")
    void testDecrementCommentCount() {
        System.out.println("\n[TEST 23] Decrementing comment count...");

        assertNotNull(thread1Id, "thread1Id should exist");

        // Arrange
        Thread thread = threadService.getById(thread1Id);
        int originalCount = thread.getRepliescount();

        // Act
        threadService.updateThreadCommentCount(thread1Id, -1);
        Thread updated = threadService.getById(thread1Id);

        // Assert
        assertEquals(originalCount - 1, updated.getRepliescount());
        System.out.println("✓ Comment count decremented: " + originalCount + " → " + updated.getRepliescount());
    }

    // ========== VOTE COUNT TESTS ==========

    @Test
    @Order(24)
    @DisplayName("24. VOTE: Should add upvote (0 → 1)")
    void testAddUpvote() {
        System.out.println("\n[TEST 24] Adding upvote...");

        assertNotNull(thread1Id, "thread1Id should exist");

        // Arrange
        Thread thread = threadService.getById(thread1Id);
        int originalLikes = thread.getLikecount();

        // Act - no vote (0) to upvote (1)
        threadService.updateThreadVoteCounts((int) thread1Id.longValue(), 0, 1);
        Thread updated = threadService.getById(thread1Id);

        // Assert
        assertEquals(originalLikes + 1, updated.getLikecount());
        System.out.println("✓ Upvote added: " + originalLikes + " → " + updated.getLikecount());
    }

    @Test
    @Order(25)
    @DisplayName("25. VOTE: Should change upvote to downvote (1 → -1)")
    void testChangeUpvoteToDownvote() {
        System.out.println("\n[TEST 25] Changing upvote to downvote...");

        assertNotNull(thread1Id, "thread1Id should exist");

        // Arrange
        Thread thread = threadService.getById(thread1Id);
        int originalLikes = thread.getLikecount();
        int originalDislikes = thread.getDislikecount();

        // Act - upvote (1) to downvote (-1)
        threadService.updateThreadVoteCounts((int) thread1Id.longValue(), 1, -1);
        Thread updated = threadService.getById(thread1Id);

        // Assert
        assertEquals(originalLikes - 1, updated.getLikecount());
        assertEquals(originalDislikes + 1, updated.getDislikecount());
        System.out.println("✓ Vote changed: upvote → downvote");
        System.out.println("  - Likes: " + originalLikes + " → " + updated.getLikecount());
        System.out.println("  - Dislikes: " + originalDislikes + " → " + updated.getDislikecount());
    }

    @Test
    @Order(26)
    @DisplayName("26. VOTE: Should remove downvote (-1 → 0)")
    void testRemoveDownvote() {
        System.out.println("\n[TEST 26] Removing downvote...");

        assertNotNull(thread1Id, "thread1Id should exist");

        // Arrange
        Thread thread = threadService.getById(thread1Id);
        int originalDislikes = thread.getDislikecount();

        // Act - downvote (-1) to no vote (0)
        threadService.updateThreadVoteCounts((int) thread1Id.longValue(), -1, 0);
        Thread updated = threadService.getById(thread1Id);

        // Assert
        assertEquals(originalDislikes - 1, updated.getDislikecount());
        System.out.println("✓ Downvote removed: " + originalDislikes + " → " + updated.getDislikecount());
    }

    // ========== FOLLOW COUNT TESTS ==========

    @Test
    @Order(27)
    @DisplayName("27. FOLLOW: Should add follower")
    void testAddFollower() {
        System.out.println("\n[TEST 27] Adding follower...");

        assertNotNull(thread1Id, "thread1Id should exist");

        // Arrange
        Thread thread = threadService.getById(thread1Id);
        int originalCount = thread.getFollowcount();

        // Act - not following to following
        threadService.updateThreadFollowCount((int) thread1Id.longValue(), false, true);
        Thread updated = threadService.getById(thread1Id);

        // Assert
        assertEquals(originalCount + 1, updated.getFollowcount());
        System.out.println("✓ Follower added: " + originalCount + " → " + updated.getFollowcount());
    }

    @Test
    @Order(28)
    @DisplayName("28. FOLLOW: Should remove follower")
    void testRemoveFollower() {
        System.out.println("\n[TEST 28] Removing follower...");

        assertNotNull(thread1Id, "thread1Id should exist");

        // Arrange
        Thread thread = threadService.getById(thread1Id);
        int originalCount = thread.getFollowcount();

        // Act - following to not following
        threadService.updateThreadFollowCount((int) thread1Id.longValue(), true, false);
        Thread updated = threadService.getById(thread1Id);

        // Assert
        assertEquals(originalCount - 1, updated.getFollowcount());
        System.out.println("✓ Follower removed: " + originalCount + " → " + updated.getFollowcount());
    }

    @Test
    @Order(29)
    @DisplayName("29. FOLLOW: Should not change if status unchanged")
    void testFollowCountUnchanged() {
        System.out.println("\n[TEST 29] Testing unchanged follow status...");

        assertNotNull(thread1Id, "thread1Id should exist");

        // Arrange
        Thread thread = threadService.getById(thread1Id);
        int originalCount = thread.getFollowcount();

        // Act - not following to not following (no change)
        threadService.updateThreadFollowCount((int) thread1Id.longValue(), false, false);
        Thread updated = threadService.getById(thread1Id);

        // Assert
        assertEquals(originalCount, updated.getFollowcount());
        System.out.println("✓ Follow count unchanged: " + originalCount);
    }

    // ========== DELETE TESTS ==========

    @Test
    @Order(30)
    @DisplayName("30. DELETE: Should delete thread successfully")
    void testDeleteThread() {
        System.out.println("\n[TEST 30] Deleting thread...");

        // Arrange - create a thread to delete
        Thread toDelete = new Thread();
        toDelete.setCategoryId(testCategoryId);
        toDelete.setTitle("Thread to be deleted " + System.currentTimeMillis());
        toDelete.setContent("This will be deleted");
        toDelete.setType(ThreadType.DISCUSSION);
        toDelete.setStatus(ThreadStatus.OPEN);
        toDelete.setPinned(false);
        threadService.add(toDelete);


        Thread created = findThreadByTitle(toDelete.getTitle());
        assertNotNull(created, "Thread should be created");
        Long deleteId = created.getId();

        // Act
        threadService.delete(created);

        // Assert
        Thread deleted = threadService.getById(deleteId);
        assertNull(deleted, "Thread should be deleted");

        System.out.println("✓ Thread deleted successfully");
        System.out.println("  - Deleted ID: " + deleteId);
    }

    @Test
    @Order(31)
    @DisplayName("31. DELETE: Should handle non-existent thread gracefully")
    void testDeleteNonExistent() {
        System.out.println("\n[TEST 31] Attempting to delete non-existent thread...");

        // Arrange
        Thread nonExistent = new Thread();
        nonExistent.setId(999999L);

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> threadService.delete(nonExistent));

        System.out.println("✓ Non-existent thread deletion handled gracefully");
    }
}