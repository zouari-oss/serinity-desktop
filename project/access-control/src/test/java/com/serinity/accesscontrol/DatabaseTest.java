/**
 * DatabaseTest.java
 *
 * JUnit test class for database operations related to {@link com.serinity.accesscontrol.model.User} 
 * and {@link com.serinity.accesscontrol.model.Profile} entities.
 *
 * <p>This class sets up an in-memory or local test database session and performs unit tests
 * for creating, reading, and validating users and profiles.</p>
 *
 * <p>Test coverage includes:</p>
 * <ul>
 *   <li>{@link #testDatabaseConnection()}   - Ensures the Hibernate session and database connection work correctly.</li>
 *   <li>{@link #testInsertUser()}           - Verifies that a {@link User} and associated {@link Profile} are correctly persisted and retrievable.</li>
 *   <li>{@link #testGetProfileByUsername()} - Checks that a {@link Profile} can be queried by its username and linked {@link User} data is correct.</li>
 * </ul>
 *
 * <p>Lifecycle:</p>
 * <ul>
 *   <li>{@link #setUp()}    - Runs before each test; initializes database state and persists a test user with profile.</li>
 *   <li>{@link #tearDown()} - Runs after each test; cleans up database tables to maintain isolation between tests.</li>
 * </ul>
 *
 * <p>Uses {@link HibernateConfig} for session factory configuration, and 
 * {@link UserRepository} / {@link ProfileRepository} for entity access.</p>
 *
 * @author  @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since   2026-02-03
 * @see     com.serinity.accesscontrol.model.User
 * @see     com.serinity.accesscontrol.model.Profile
 * @see     com.serinity.accesscontrol.config.HibernateConfig
 *
 * <a href="https://github.com/zouari-oss/serinity-desktop/blob/main/project/access-control/src/test/java/com/serinity/accesscontrol/DatabaseTest.java"
 * target="_blank">
 * DatabaseTest.java
 * </a>
 */

// `DatabaseTest` package name
package com.serinity.accesscontrol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

// `java` import(s)
import java.util.UUID;

// `hibernate` import(s)
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
// `junit` import(s)
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// `serinity` imports
import com.serinity.accesscontrol.config.HibernateConfig;
import com.serinity.accesscontrol.flag.Role;
import com.serinity.accesscontrol.model.Profile;
import com.serinity.accesscontrol.model.User;
import com.serinity.accesscontrol.repository.ProfileRepository;

public final class DatabaseTest {
  private final String email = "email_" + UUID.randomUUID() + "@example.com", password = "test";
  private String username;

  @BeforeEach
  void setUp() {
    Session session = HibernateConfig.getSessionFactory().openSession();
    Transaction tx = session.beginTransaction();
    session.createQuery("delete from Profile").executeUpdate();
    session.createQuery("delete from User").executeUpdate();

    // Create user
    User user = new User();
    user.setEmail(email);
    user.setPasswordHash(password);
    user.setRole(Role.PATIENT);

    // Create profile (username auto-generated)
    Profile profile = new Profile();
    profile.setUser(user);
    profile.setUsername("user_" + UUID.randomUUID());
    user.setProfile(profile);

    session.persist(user); // then persist user

    tx.commit();
    username = user.getProfile().getUsername(); // Fetch generated username
    session.close();
  }

  @Test
  public void testDatabaseConnection() {
    SessionFactory sessionFactory = HibernateConfig.getSessionFactory();
    assertNotNull(sessionFactory, "SessionFactory should not be null");

    try (Session session = sessionFactory.openSession()) {
      assertNotNull(session, "Session should not be null");

      // Use typed native query to avoid deprecation
      Transaction tx = session.beginTransaction();
      Integer result = session.createNativeQuery("SELECT 1", Integer.class)
          .getSingleResult();
      tx.commit();

      assertNotNull(result, "Query result should not be null");
      System.out.println("DB connection test passed! Query result: " + result);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Failed to connect to the database: " + e.getMessage());
    }
  }

  @Test
  public void testInsertUser() {
    Profile profile = ProfileRepository.findByUsername(username);
    assertNotNull(profile, "Profile should exist for username: " + username);

    User user = profile.getUser();
    assertNotNull(user, "User should exist for profile: " + username);
    assertNotNull(user.getId(), "User ID should be generated after persist");
    assertEquals(email, user.getEmail(), "User email should match");
  }

  @Test
  public void testGetProfileByUsername() {
    Profile profile = ProfileRepository.findByUsername(username);
    assertNotNull(profile, "Profile should exist - " + username);
    assertEquals(email, profile.getUser().getEmail(), "Profile's user email should match");
  }

  @AfterEach
  void tearDown() {
    Session session = HibernateConfig.getSessionFactory().openSession();
    Transaction tx = session.beginTransaction();
    session.createQuery("delete from Profile").executeUpdate();
    session.createQuery("delete from User").executeUpdate();
    tx.commit();
    session.close();
  }
} // DatabaseTest class
