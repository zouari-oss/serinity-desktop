// `ProfileTest` package name
package com.serinity.accesscontrol;

// `junit` static import(s)
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// `java` import(s)
import java.util.UUID;

// `junit` import(s)
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.zouarioss.skinnedratorm.core.EntityManager;

// `serinity` import(s)
import com.serinity.accesscontrol.config.SkinnedRatOrmEntityManager;
import com.serinity.accesscontrol.flag.Gender;
import com.serinity.accesscontrol.flag.UserRole;
import com.serinity.accesscontrol.model.Profile;
import com.serinity.accesscontrol.model.User;
import com.serinity.accesscontrol.repository.ProfileRepository;

/**
 * JUnit test class for {@link com.serinity.accesscontrol.model.Profile} entity
 * operations.
 *
 * <p>
 * This class tests Profile-specific functionality including CRUD operations and
 * profile data validation.
 * </p>
 *
 * <p>
 * Test coverage includes:
 * </p>
 * <ul>
 * <li>{@link #testFindProfileByUsername()} - Verifies profile retrieval by
 * username.</li>
 * <li>{@link #testUpdateProfile()} - Checks profile update operations.</li>
 * <li>{@link #testProfileWithCompleteInformation()} - Validates complete
 * profile data persistence.</li>
 * </ul>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-12
 * @see com.serinity.accesscontrol.model.Profile
 * @see com.serinity.accesscontrol.repository.ProfileRepository
 *
 *      <a href=
 *      "https://github.com/zouari-oss/serinity-desktop/blob/main/project/access-control/src/test/java/com/serinity/accesscontrol/ProfileTest.java">
 *      ProfileTest.java
 *      </a>
 */
public final class ProfileTest {
  private EntityManager em;
  private ProfileRepository profileRepo;
  private User testUser;
  private Profile testProfile;

  @Test
  @Order(1)
  public void testFindProfileByUsername() throws Exception {
    final Profile retrievedProfile = profileRepo.findByUsername(testProfile.getUsername());

    assertNotNull(retrievedProfile, "Profile should exist for username: " + testProfile.getUsername());
    assertNotNull(retrievedProfile.getUser(), "User should exist for profile");
    assertEquals(testUser.getEmail(), retrievedProfile.getUser().getEmail(), "User email should match");
  }

  @Test
  @Order(2)
  public void testUpdateProfile() throws Exception {
    final String newFirstName = "John";
    final String newLastName = "Doe";

    testProfile.setFirstName(newFirstName);
    testProfile.setLastName(newLastName);
    em.update(testProfile);

    final Profile updatedProfile = profileRepo.findByUsername(testProfile.getUsername());
    assertEquals(newFirstName, updatedProfile.getFirstName(), "First name should be updated");
    assertEquals(newLastName, updatedProfile.getLastName(), "Last name should be updated");
  }

  @Test
  @Order(3)
  public void testProfileWithCompleteInformation() throws Exception {
    final User user = createTestUser();
    final Profile profile = createCompleteProfile(user);

    profileRepo.save(profile);

    final Profile retrievedProfile = profileRepo.findByUsername(profile.getUsername());
    assertNotNull(retrievedProfile.getFirstName(), "First name should be set");
    assertNotNull(retrievedProfile.getLastName(), "Last name should be set");
    assertNotNull(retrievedProfile.getPhone(), "Phone should be set");
    assertNotNull(retrievedProfile.getCountry(), "Country should be set");
    assertNotNull(retrievedProfile.getState(), "State should be set");

    em.delete(user);
    em.delete(profile);
  }

  @BeforeEach
  void setUp() throws Exception {
    em = SkinnedRatOrmEntityManager.getEntityManager();
    profileRepo = new ProfileRepository(em);

    testUser = createTestUser();
    testProfile = createTestProfile(testUser);

    profileRepo.save(testProfile);
  }

  @AfterEach
  void tearDown() throws Exception {
    em.delete(testUser);
    em.delete(testProfile);
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

  private Profile createCompleteProfile(final User user) {
    final Profile profile = createTestProfile(user);
    profile.setFirstName("Jane");
    profile.setLastName("Smith");
    profile.setPhone("+1234567890");
    profile.setCountry("Tunisia");
    profile.setState("Sfax");
    profile.setAboutMe("Test user profile");
    profile.setGender(Gender.FEMALE);
    return profile;
  }

  private String generateUniqueEmail() {
    return "email_" + UUID.randomUUID() + "@example.com";
  }

  private String generateUniqueUsername() {
    return "user_" + UUID.randomUUID();
  }
} // ProfileTest test class
