// `DatabaseTest` package name
package com.serinity.accesscontrol;

// `junit` static import(s)
import static org.junit.jupiter.api.Assertions.assertNotNull;

// `junit` import(s)
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.zouarioss.skinnedratorm.core.EntityManager;

// `serinity` import(s)
import com.serinity.accesscontrol.config.SkinnedRatOrmEntityManager;

/**
 * JUnit test class for basic database connectivity and configuration.
 *
 * <p>
 * This class tests core database infrastructure including connection and entity
 * manager setup.
 * </p>
 *
 * <p>
 * Test coverage includes:
 * </p>
 * <ul>
 * <li>{@link #testDatabaseConnection()} - Ensures the entity manager and
 * database connection work correctly.</li>
 * </ul>
 *
 * <p>
 * Note: Specific User and Profile tests have been moved to {@link UserTest} and
 * {@link ProfileTest}.
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 2.0
 * @since 2026-02-03
 * @see com.serinity.accesscontrol.config.SkinnedRatOrmEntityManager
 * @see UserTest
 * @see ProfileTest
 *
 *      <a href=
 *      "https://github.com/zouari-oss/serinity-desktop/blob/main/project/access-control/src/test/java/com/serinity/accesscontrol/DatabaseTest.java">
 *      DatabaseTest.java
 *      </a>
 */
public final class DatabaseTest {
  private EntityManager em;

  @Test
  @Order(1)
  public void testDatabaseConnection() {
    assertNotNull(em, "EntityManager should not be null");
  }

  @BeforeEach
  void setUp() throws Exception {
    em = SkinnedRatOrmEntityManager.getEntityManager();
  }
} // DatabaseTest test class
