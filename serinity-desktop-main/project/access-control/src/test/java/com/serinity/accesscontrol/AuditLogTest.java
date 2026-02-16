/**
 * AuditLogTest.java
 *
 * JUnit test class for audit log operations related to 
 * {@link com.serinity.accesscontrol.model.AuditLog} entity.
 *
 * <p>This class tests audit logging functionality including action tracking,
 * system information capture, and session association.</p>
 *
 * <p>Test coverage includes:</p>
 * <ul>
 *   <li>{@link #testCreateAuditLog()}           - Verifies audit log creation with action.</li>
 *   <li>{@link #testAuditLogAutoFields()}       - Ensures system info is auto-captured.</li>
 *   <li>{@link #testAuditLogWithSession()}      - Tests association with auth session.</li>
 *   <li>{@link #testMultipleAuditLogs()}        - Validates multiple logs per session.</li>
 *   <li>{@link #testAuditLogTimestamp()}        - Checks timestamp auto-generation.</li>
 * </ul>
 *
 * <p>Uses SOLID principles:</p>
 * <ul>
 *   <li>Single Responsibility - Separate test data builders for different entities.</li>
 *   <li>Dependency Inversion  - Dependencies injected through constructors.</li>
 *   <li>Open/Closed           - Builder methods can be extended without modification.</li>
 * </ul>
 *
 * @author  @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since   2026-02-12
 * @see     com.serinity.accesscontrol.model.AuditLog
 * @see     com.serinity.accesscontrol.model.AuthSession
 *
 * <a href="https://github.com/zouari-oss/serinity-desktop/blob/main/project/access-control/src/test/java/com/serinity/accesscontrol/AuditLogTest.java"
 * target="_blank">
 * AuditLogTest.java
 * </a>
 */

// `AuditLogTest` package name
package com.serinity.accesscontrol;

// `junit` static import(s)
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// `java` import(s)
import java.time.Instant;
import java.util.UUID;

// `junit` import(s)
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.zouarioss.skinnedratorm.core.EntityManager;

// `serinity` imports
import com.serinity.accesscontrol.config.SkinnedRatOrmEntityManager;
import com.serinity.accesscontrol.flag.UserRole;
import com.serinity.accesscontrol.model.AuditLog;
import com.serinity.accesscontrol.model.AuthSession;
import com.serinity.accesscontrol.model.Profile;
import com.serinity.accesscontrol.model.User;
import com.serinity.accesscontrol.repository.ProfileRepository;

public final class AuditLogTest {
  private EntityManager em;
  private ProfileRepository profileRepo;
  private User testUser;
  private Profile testProfile;
  private AuthSession testSession;
  private AuditLog testAuditLog;

  @Test
  @Order(1)
  public void testCreateAuditLog() {
    assertNotNull(testAuditLog.getId(), "Audit log ID should be generated");
    assertEquals("USER_LOGIN", testAuditLog.getAction(), "Action should match");
  }

  @Test
  @Order(2)
  public void testAuditLogAutoFields() {
    assertNotNull(testAuditLog.getPrivateIpAddress(), "IP address should be auto-captured");
    assertNotNull(testAuditLog.getOsName(), "OS name should be auto-captured");
    assertNotNull(testAuditLog.getHostname(), "Hostname should be auto-captured");
  }

  @Test
  @Order(3)
  public void testAuditLogWithSession() throws Exception {
    final AuditLog logWithSession = createTestAuditLog("PASSWORD_CHANGE");
    logWithSession.setSession(testSession);
    em.persist(logWithSession);

    assertNotNull(logWithSession.getSession(), "Session should be associated");
    assertEquals(testSession.getId(), logWithSession.getSession().getId(), "Session ID should match");

    em.delete(logWithSession);
  }

  @Test
  @Order(4)
  public void testMultipleAuditLogs() throws Exception {
    final AuditLog secondLog = createTestAuditLog("PROFILE_UPDATE");
    final AuditLog thirdLog = createTestAuditLog("USER_LOGOUT");

    em.persist(secondLog);
    em.persist(thirdLog);

    assertNotNull(secondLog.getId(), "Second log should be created");
    assertNotNull(thirdLog.getId(), "Third log should be created");

    em.delete(secondLog);
    em.delete(thirdLog);
  }

  @Test
  @Order(5)
  public void testAuditLogTimestamp() {
    assertNotNull(testAuditLog.getCreatedAt(), "Timestamp should be auto-generated");
    assertTrue(testAuditLog.getCreatedAt().isBefore(Instant.now().plusSeconds(1)),
        "Timestamp should be recent");
  }

  @Test
  @Order(6)
  public void testAuditLogActionTypes() throws Exception {
    final String[] actions = { "USER_LOGIN", "USER_LOGOUT", "PASSWORD_RESET", "PROFILE_UPDATE", "EMAIL_CHANGE" };

    for (final String action : actions) {
      final AuditLog log = createTestAuditLog(action);
      em.persist(log);
      assertEquals(action, log.getAction(), "Action should match: " + action);
      em.delete(log);
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    em = SkinnedRatOrmEntityManager.getEntityManager();
    profileRepo = new ProfileRepository(em);

    testUser = createTestUser();
    testProfile = createTestProfile(testUser);
    profileRepo.save(testProfile);

    testSession = createTestAuthSession(testUser);
    em.persist(testSession);

    testAuditLog = createTestAuditLog("USER_LOGIN");
    em.persist(testAuditLog);
  }

  @AfterEach
  void tearDown() throws Exception {
    em.delete(testAuditLog);
    em.delete(testSession);
    em.delete(testUser);
    em.delete(testProfile);
  }

  // ##########################
  // ### TEST DATA BUILDERS ###
  // ##########################

  /**
   * Creates a test user with default values.
   * Follows Single Responsibility Principle.
   */
  private User createTestUser() {
    final User user = new User();
    user.setEmail(generateUniqueEmail());
    user.setPasswordHash("hashedPassword123");
    user.setRole(UserRole.PATIENT);
    return user;
  }

  /**
   * Creates a test profile for the given user.
   * Follows Dependency Inversion Principle.
   */
  private Profile createTestProfile(final User user) {
    final Profile profile = new Profile();
    profile.setUsername(generateUniqueUsername());
    profile.setUser(user);
    return profile;
  }

  /**
   * Creates a test auth session for the given user.
   * Follows Single Responsibility Principle.
   */
  private AuthSession createTestAuthSession(final User user) {
    final AuthSession session = new AuthSession();
    session.setRefreshToken(generateUniqueToken());
    session.setUser(user);
    return session;
  }

  /**
   * Creates a test audit log with the specified action.
   * Follows Open/Closed Principle - can be extended for different log types.
   */
  private AuditLog createTestAuditLog(final String action) {
    final AuditLog log = new AuditLog();
    log.setAction(action);
    return log;
  }

  private String generateUniqueEmail() {
    return "email_" + UUID.randomUUID() + "@example.com";
  }

  private String generateUniqueUsername() {
    return "user_" + UUID.randomUUID();
  }

  private String generateUniqueToken() {
    return "token_" + UUID.randomUUID();
  }
} // AuditLogTest test class
