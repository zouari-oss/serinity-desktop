package com.serinity.forumcontrol.Services;

import com.serinity.forumcontrol.Models.Reply;
import com.serinity.forumcontrol.Models.Thread;
import com.serinity.forumcontrol.Models.ThreadStatus;
import com.serinity.forumcontrol.Models.ThreadType;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServiceReplyTest {

    private ServiceReply serviceReply;
    private ServiceThread serviceThread;

    private long testThreadId;
    private long parentReplyId;

    @BeforeAll
    void setup() {
        serviceReply = new ServiceReply();
        serviceThread = new ServiceThread();

        // Create test thread
        Thread thread = new Thread();
        thread.setCategoryId(1L); // ensure category 1 exists
        thread.setTitle("Reply Test Thread");
        thread.setContent("Testing replies...");
        thread.setType(ThreadType.DISCUSSION);
        thread.setStatus(ThreadStatus.OPEN);
        thread.setPinned(false);

        serviceThread.add(thread);

        Thread saved = serviceThread.getAll().stream()
                .filter(t -> t.getTitle().equals("Reply Test Thread"))
                .findFirst()
                .orElse(null);

        assertNotNull(saved);
        testThreadId = saved.getId();

        System.out.println("=== Starting ServiceReply Tests ===");
    }

    @AfterAll
    void cleanup() {
        serviceReply.deleteByThread(testThreadId);

        Thread thread = serviceThread.getById(testThreadId);
        if (thread != null) {
            serviceThread.delete(thread);
        }

        System.out.println("=== ServiceReply Tests Completed ===");
    }

    // ================= ADD =================

    @Test
    @Order(1)
    @DisplayName("Should add top-level reply")
    void testAddReply() {

        Reply reply = new Reply();
        reply.setThreadId(testThreadId);
        reply.setContent("This is a top-level reply");

        serviceReply.add(reply);

        List<Reply> replies = serviceReply.getTopLevelReplies(testThreadId);

        assertFalse(replies.isEmpty());

        parentReplyId = replies.get(0).getId();

        System.out.println("✓ Top-level reply added");
    }

    @Test
    @Order(2)
    @DisplayName("Should add nested reply")
    void testAddNestedReply() {

        Reply nested = new Reply();
        nested.setThreadId(testThreadId);
        nested.setParentId(parentReplyId);
        nested.setContent("This is a nested reply");

        serviceReply.add(nested);

        List<Reply> nestedReplies =
                serviceReply.getNestedReplies(parentReplyId);

        assertFalse(nestedReplies.isEmpty());

        System.out.println("✓ Nested reply added");
    }

    // ================= GET =================

    @Test
    @Order(3)
    @DisplayName("Should get reply by ID")
    void testGetById() {

        Reply reply = serviceReply.getById(parentReplyId);

        assertNotNull(reply);
        assertEquals("This is a top-level reply", reply.getContent());

        System.out.println("✓ Get by ID successful");
    }

    @Test
    @Order(4)
    @DisplayName("Should get replies by user")
    void testGetByUser() {

        List<Reply> replies = serviceReply.getByUser(
                serviceReply.getById(parentReplyId).getUserId()
        );

        assertFalse(replies.isEmpty());

        System.out.println("✓ Get by user successful");
    }

    @Test
    @Order(5)
    @DisplayName("Should get top-level replies")
    void testGetTopLevelReplies() {

        List<Reply> replies =
                serviceReply.getTopLevelReplies(testThreadId);

        assertTrue(replies.stream()
                .anyMatch(r -> r.getParentId() == null));

        System.out.println("✓ Get top-level replies successful");
    }

    @Test
    @Order(6)
    @DisplayName("Should get nested replies")
    void testGetNestedReplies() {

        List<Reply> replies =
                serviceReply.getNestedReplies(parentReplyId);

        assertFalse(replies.isEmpty());

        System.out.println("✓ Get nested replies successful");
    }

    // ================= UPDATE =================

    @Test
    @Order(7)
    @DisplayName("Should update reply")
    void testUpdateReply() {

        Reply reply = serviceReply.getById(parentReplyId);
        reply.setContent("Updated content");

        serviceReply.update(reply);

        Reply updated = serviceReply.getById(parentReplyId);

        assertEquals("Updated content", updated.getContent());

        System.out.println("✓ Reply updated successfully");
    }

    @Test
    @Order(8)
    @DisplayName("Should update content only")
    void testUpdateContentOnly() {

        serviceReply.updateContent(parentReplyId, "Updated again");

        Reply updated = serviceReply.getById(parentReplyId);

        assertEquals("Updated again", updated.getContent());

        System.out.println("✓ Content-only update successful");
    }

    // ================= AUTHOR =================

    @Test
    @Order(9)
    @DisplayName("Should get reply author")
    void testGetReplyAuthor() {

        Reply reply = serviceReply.getById(parentReplyId);
        String username = serviceReply.getReplyAuthor(reply.getUserId());

        assertNotNull(username);

        System.out.println("✓ Reply author retrieved: " + username);
    }

    // ================= DELETE =================

    @Test
    @Order(10)
    @DisplayName("Should delete nested replies")
    void testDeleteNestedReplies() {

        serviceReply.deleteNestedReplies(parentReplyId);

        List<Reply> nested =
                serviceReply.getNestedReplies(parentReplyId);

        assertTrue(nested.isEmpty());

        System.out.println("✓ Nested replies deleted");
    }

    @Test
    @Order(11)
    @DisplayName("Should delete reply")
    void testDeleteReply() {

        Reply reply = serviceReply.getById(parentReplyId);

        serviceReply.delete(reply);

        assertNull(serviceReply.getById(parentReplyId));

        System.out.println("✓ Reply deleted successfully");
    }

    @Test
    @Order(12)
    @DisplayName("Should handle non-existent reply gracefully")
    void testDeleteNonExistentReply() {

        Reply fake = new Reply();
        fake.setId(999999);
        fake.setThreadId(testThreadId);

        assertDoesNotThrow(() -> serviceReply.delete(fake));

        System.out.println("✓ Non-existent reply handled safely");
    }

    @Test
    @Order(13)
    @DisplayName("Should delete all replies by thread")
    void testDeleteByThread() {

        // Add temporary reply
        Reply reply = new Reply();
        reply.setThreadId(testThreadId);
        reply.setContent("Temp reply");
        serviceReply.add(reply);

        serviceReply.deleteByThread(testThreadId);

        List<Reply> replies =
                serviceReply.getTopLevelReplies(testThreadId);

        assertTrue(replies.isEmpty());

        System.out.println("✓ Delete by thread successful");
    }
}
