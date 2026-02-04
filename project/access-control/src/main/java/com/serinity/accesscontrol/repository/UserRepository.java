/**
 * UserRepository.java
 *
 * Repository class for performing CRUD operations on {@link com.serinity.accesscontrol.model.User} entities.
 *
 * @author  @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since   2026-02-03
 * @see     com.serinity.accesscontrol.model.User
 * @see     com.serinity.accesscontrol.repository.ProfileRepository
 *
 * <a
 * href="https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/repository/UserRepository.java"
 * target="_blank">
 * UserRepository.java
 * </a>
 */

// `UserRepository` package name
package com.serinity.accesscontrol.repository;

// `hibernate` import(s)
import org.hibernate.Session;
import org.hibernate.Transaction;

// `serinity` import(s)
import com.serinity.accesscontrol.config.HibernateConfig;
import com.serinity.accesscontrol.model.User;

/**
 * Brief summary of the method.
 *
 * <p>
 * This class provides methods to create, read, update, and delete users in the
 * database using Hibernate ORM.
 * It abstracts database interactions for the User entity and allows querying
 * users by ID or associated profile username.
 * </p>
 *
 * @see com.serinity.accesscontrol.config.HibernateConfig#getSessionFactory
 *
 *      <pre>
 * {@code
 * // Example usage
 * User user = new User();
 * user.setEmail("example@email.com");
 * user.setPasswordHash("password123");
 * UserRepository.save(user);
 * User foundUser = UserRepository.findByUsername("username123");
 * }</pre>
 */
public final class UserRepository {
  public static void save(final User user) {
    Transaction tx = null;
    try (Session session = HibernateConfig.getSessionFactory().openSession()) {
      tx = session.beginTransaction();
      session.persist(user);
      tx.commit();
    } catch (final Exception e) {
      if (tx != null)
        tx.rollback();
      throw e;
    }
  }
} // UserRepository final class
