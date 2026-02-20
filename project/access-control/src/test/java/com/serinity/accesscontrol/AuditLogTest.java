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
import org.junit.jupiter.api.TestInstance;
import org.zouarioss.skinnedratorm.core.EntityManager;

// `serinity` imports
import com.serinity.accesscontrol.config.SkinnedRatOrmEntityManager;
import com.serinity.accesscontrol.flag.UserRole;
import com.serinity.accesscontrol.model.AuditLog;
import com.serinity.accesscontrol.model.AuthSession;
import com.serinity.accesscontrol.model.Profile;
import com.serinity.accesscontrol.model.User;
import com.serinity.accesscontrol.repository.AuditLogRepository;
import com.serinity.accesscontrol.repository.AuthSessionRepository;
import com.serinity.accesscontrol.repository.ProfileRepository;
import com.serinity.accesscontrol.repository.UserRepository;
import com.serinity.accesscontrol.util.PasswordEncoder;

/**
 * JUnit test class for audit log operations related to
 * {@link com.serinity.accesscontrol.model.AuditLog} entity.
 *
 * <p>
 * This class tests audit logging functionality including action tracking,
 * system information capture, and session association.
 * </p>
 *
 * <p>
 * Test coverage includes:
 * </p>
 * <ul>
 * <li>{@link #testCreateAuditLog()} - Verifies audit log creation and action
 * value.</li>
 * <li>{@link #testAuditLogAutoFields()} - Ensures system information (IP, OS,
 * hostname) is auto-captured.</li>
 * <li>{@link #testAuditLogWithSession()} - Tests association between audit log
 * and authentication session.</li>
 * <li>{@link #testMultipleAuditLogs()} - Validates multiple audit logs can be
 * created for the same session.</li>
 * <li>{@link #testAuditLogTimestamp()} - Checks automatic timestamp
 * generation.</li>
 * <li>{@link #testAuditLogActionTypes()} - Verifies support for multiple audit
 * action types.</li>
 * </ul>
 *
 * <p>
 * Uses SOLID principles:
 * </p>
 * <ul>
 * <li>Single Responsibility - Separate test data builders for different
 * entities.</li>
 * <li>Dependency Inversion - Dependencies injected through constructors.</li>
 * <li>Open/Closed - Builder methods can be extended without modification.</li>
 * </ul>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-12
 * @see com.serinity.accesscontrol.model.AuditLog
 * @see com.serinity.accesscontrol.model.AuthSession
 *
 *      <a href=
 *      "https://github.com/zouari-oss/serinity-desktop/blob/main/project/access-control/src/test/java/com/serinity/accesscontrol/AuditLogTest.java">
 *      AuditLogTest.java
 *      </a>
 */
public final class AuditLogTest {
  // Entity manager
  private EntityManager em;

  // Respositories
  private UserRepository userRepository;
  private AuthSessionRepository authSessionRepository;
  private AuditLogRepository auditLogRepository;

  // Entities
  private User testUser;
  private AuthSession testSession;
  private AuditLog testAuditLog;

  @BeforeEach
  void setUp() {
    em = SkinnedRatOrmEntityManager.getEntityManager();

    userRepository = new UserRepository(em);
    authSessionRepository = new AuthSessionRepository(em);
    auditLogRepository = new AuditLogRepository(em);

    // Persist user
    testUser = createTestUser();
    userRepository.save(testUser);

    // Persist session
    testSession = createTestAuthSession(testUser);
    authSessionRepository.save(testSession);

    // Persist audit log
    testAuditLog = createTestAuditLog("USER_LOGIN");
    testAuditLog.setSession(testSession);
    auditLogRepository.save(testAuditLog);
  }

  @AfterEach
  void tearDown() throws Exception {
    if (testAuditLog != null) {
      em.delete(testAuditLog);
    }

    if (testSession != null) {
      em.delete(testSession);
    }

    if (testUser != null) {
      em.delete(testUser);
    }
  }

  // ======================
  // ======= TESTS ========
  // ======================

  @Test
  void testCreateAuditLog() {
    assertNotNull(testAuditLog.getId());
    assertEquals("USER_LOGIN", testAuditLog.getAction());
  }

  @Test
  void testAuditLogAutoFields() {
    assertNotNull(testAuditLog.getPrivateIpAddress());
    assertNotNull(testAuditLog.getOsName());
    assertNotNull(testAuditLog.getHostname());
  }

  @Test
  void testAuditLogWithSession() {

    final AuditLog log = createTestAuditLog("PASSWORD_CHANGE");
    log.setSession(testSession);
    auditLogRepository.save(log);

    assertNotNull(log.getSession());
    assertEquals(testSession.getId(), log.getSession().getId());
  }

  @Test
  void testMultipleAuditLogs() {

    final AuditLog secondLog = createTestAuditLog("PROFILE_UPDATE");
    secondLog.setSession(testSession);

    final AuditLog thirdLog = createTestAuditLog("USER_LOGOUT");
    thirdLog.setSession(testSession);

    auditLogRepository.save(secondLog);
    auditLogRepository.save(thirdLog);

    assertNotNull(secondLog.getId());
    assertNotNull(thirdLog.getId());
  }

  @Test
  void testAuditLogTimestamp() {
    assertNotNull(testAuditLog.getCreatedAt());
    assertTrue(testAuditLog.getCreatedAt().isBefore(Instant.now().plusSeconds(1)));
  }

  @Test
  void testAuditLogActionTypes() {
    final String[] actions = {
        "USER_LOGIN",
        "USER_LOGOUT",
        "PASSWORD_RESET",
        "PROFILE_UPDATE",
        "EMAIL_CHANGE"
    };

    for (final String action : actions) {
      final AuditLog log = createTestAuditLog(action);
      log.setSession(testSession);
      auditLogRepository.save(log);

      assertEquals(action, log.getAction());
    }
  }

  // ======================
  // ==== BUILDERS ========
  // ======================

  private User createTestUser() {
    final User user = new User();
    user.setEmail("email_" + UUID.randomUUID() + "@example.com");
    user.setPasswordHash(PasswordEncoder.encode("hashedPassword123"));
    user.setRole(UserRole.PATIENT);
    return user;
  }

  private AuthSession createTestAuthSession(final User user) {
    final AuthSession session = new AuthSession();
    session.setUser(user);
    return session;
  }

  private AuditLog createTestAuditLog(final String action) {
    final AuditLog log = new AuditLog();
    log.setAction(action);
    return log;
  }
} // AuditLogTest test class
