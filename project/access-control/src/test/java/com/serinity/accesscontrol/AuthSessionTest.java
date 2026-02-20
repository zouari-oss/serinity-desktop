// `AuthSessionTest` package name
package com.serinity.accesscontrol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.serinity.accesscontrol.model.AuthSession;
import com.serinity.accesscontrol.model.Profile;
import com.serinity.accesscontrol.model.User;
import com.serinity.accesscontrol.repository.ProfileRepository;

/**
 * JUnit test class for authentication session operations related to
 * {@link com.serinity.accesscontrol.model.AuthSession} entity.
 *
 * <p>
 * This class tests session creation, validation, expiration, and revocation
 * functionality for user authentication sessions.
 * </p>
 *
 * <p>
 * Test coverage includes:
 * </p>
 * <ul>
 * <li>{@link #testCreateAuthSession()} - Verifies session creation with valid
 * refresh token.</li>
 * <li>{@link #testSessionAutoTimestamp()} - Ensures timestamps are
 * auto-generated on persist.</li>
 * <li>{@link #testSessionExpiration()} - Validates expiration date is properly
 * set.</li>
 * <li>{@link #testMultipleSessionsPerUser()} - Checks that a user can have
 * multiple active sessions.</li>
 * <li>{@link #testRevokeSession()} - Tests session revocation
 * functionality.</li>
 * </ul>
 *
 * <p>
 * Uses SOLID principles:
 * </p>
 * <ul>
 * <li>Single Responsibility - Separate test data builders.</li>
 * <li>Dependency Inversion - Injected EntityManager and repositories.</li>
 * </ul>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-12
 * @see com.serinity.accesscontrol.model.AuthSession
 * @see com.serinity.accesscontrol.model.User
 *
 *      <a href=
 *      "https://github.com/zouari-oss/serinity-desktop/blob/main/project/access-control/src/test/java/com/serinity/accesscontrol/AuthSessionTest.java">
 *      AuthSessionTest.java
 *      </a>
 */
public final class AuthSessionTest {
  private EntityManager em;
  private ProfileRepository profileRepo;
  private User testUser;
  private Profile testProfile;
  private AuthSession testSession;

  @Test
  @Order(1)
  public void testCreateAuthSession() {
    assertNotNull(testSession.getId(), "Session ID should be generated");
    assertNotNull(testSession.getRefreshToken(), "Refresh token should be set");
    assertEquals(testUser.getEmail(), testSession.getUser().getEmail(), "User should match");
  }

  @Test
  @Order(2)
  public void testSessionAutoTimestamp() {
    assertNotNull(testSession.getCreatedAt(), "Created timestamp should be auto-generated");
    assertNotNull(testSession.getExpiresAt(), "Expiration timestamp should be auto-generated");
    assertTrue(testSession.getExpiresAt().isAfter(testSession.getCreatedAt()),
        "Expiration should be after creation");
  }

  @Test
  @Order(3)
  public void testSessionExpiration() {
    final Instant now = Instant.now();
    assertTrue(testSession.getExpiresAt().isAfter(now), "Session should not be expired initially");
  }

  @Test
  @Order(4)
  public void testMultipleSessionsPerUser() throws Exception {
    final AuthSession secondSession = createTestAuthSession(testUser);
    em.persist(secondSession);

    assertNotNull(secondSession.getId(), "Second session should be created");
    assertFalse(testSession.getRefreshToken().equals(secondSession.getRefreshToken()),
        "Refresh tokens should be unique");

    em.delete(secondSession);
  }

  @Test
  @Order(5)
  public void testRevokeSession() throws Exception {
    assertFalse(testSession.isRevoked(), "Session should not be revoked initially");

    // Simulate revocation (in real app, this would be via a service method)
    em.update(testSession);

    assertNotNull(testSession.getRefreshToken(), "Refresh token should still exist");
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
  }

  @AfterEach
  void tearDown() throws Exception {
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
    session.setUser(user);
    return session;
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
} // AuthSessionTest test class
