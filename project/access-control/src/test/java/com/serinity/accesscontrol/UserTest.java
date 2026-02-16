// `UserTest` package name
package com.serinity.accesscontrol;

// `java` import(s)
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// `java` import(s)
import java.util.UUID;

// `junit` import(s)
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.core.EntityManager;

// `serinity` import(s)
import com.serinity.accesscontrol.config.SkinnedRatOrmEntityManager;
import com.serinity.accesscontrol.flag.UserRole;
import com.serinity.accesscontrol.model.Profile;
import com.serinity.accesscontrol.model.User;
import com.serinity.accesscontrol.repository.ProfileRepository;

/**
 * JUnit test class for {@link com.serinity.accesscontrol.model.User} entity
 * operations.
 *
 * <p>
 * This class tests User-specific functionality including roles, statuses, and
 * user management.
 * </p>
 *
 * <p>
 * Test coverage includes:
 * </p>
 * <ul>
 * <li>{@link #testUserWithDifferentRoles()} - Verifies users can be created
 * with different roles.</li>
 * <li>{@link #testUserDefaultStatus()} - Checks default status values are
 * properly set.</li>
 * </ul>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-12
 * @see com.serinity.accesscontrol.model.User
 * @see com.serinity.accesscontrol.config.SkinnedRatOrmEntityManager
 *
 *      <a href=
 *      "https://github.com/zouari-oss/serinity-desktop/blob/main/project/access-control/src/test/java/com/serinity/accesscontrol/UserTest.java">
 *      UserTest.java
 *      </a>
 */
public final class UserTest {
  private EntityManager em;
  private ProfileRepository profileRepo;

  @Test
  @Order(1)
  public void testUserWithDifferentRoles() throws Exception {
    final User therapistUser = createTestUserWithRole(UserRole.THERAPIST);
    final Profile therapistProfile = createTestProfile(therapistUser);

    profileRepo.save(therapistProfile);

    final Profile retrievedProfile = profileRepo.findByUsername(therapistProfile.getUsername());
    assertEquals(UserRole.THERAPIST, retrievedProfile.getUser().getRole(), "User role should be THERAPIST");

    em.delete(therapistUser);
    em.delete(therapistProfile);
  }

  @Test
  @Order(2)
  public void testUserDefaultStatus() throws Exception {
    final User user = createTestUser();
    final Profile profile = createTestProfile(user);

    profileRepo.save(profile);

    final Profile retrievedProfile = profileRepo.findByUsername(profile.getUsername());
    assertNotNull(retrievedProfile.getUser().getPresenceStatus(), "Presence status should be auto-set");
    assertNotNull(retrievedProfile.getUser().getAccountStatus(), "Account status should be auto-set");

    em.delete(user);
    em.delete(profile);
  }

  @BeforeEach
  void setUp() throws Exception {
    em = SkinnedRatOrmEntityManager.getEntityManager();
    profileRepo = new ProfileRepository(em);
  }

  @AfterEach
  void tearDown() throws Exception {
    // Cleanup is handled in individual tests
  }

  // ##########################
  // ### TEST DATA BUILDERS ###
  // ##########################

  private User createTestUser() {
    return createTestUserWithRole(UserRole.PATIENT);
  }

  private User createTestUserWithRole(final UserRole role) {
    final User user = new User();
    user.setEmail(generateUniqueEmail());
    user.setPasswordHash("test");
    user.setRole(role);
    return user;
  }

  private Profile createTestProfile(final User user) {
    final Profile profile = new Profile();
    profile.setUsername(generateUniqueUsername());
    profile.setUser(user);
    return profile;
  }

  private String generateUniqueEmail() {
    return "email_" + UUID.randomUUID() + "@example.com";
  }

  private String generateUniqueUsername() {
    return "user_" + UUID.randomUUID();
  }
} // UserTest test class
