// `UserRepository` package name
package com.serinity.accesscontrol.repository;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.core.EntityManager;

// `serinity` import(s)
import com.serinity.accesscontrol.model.User;
import com.serinity.accesscontrol.repository.base.BaseRepository;

/**
 * Repository class for performing CRUD operations on
 * {@link com.serinity.accesscontrol.model.User} entities.
 *
 * <p>
 * This class provides methods to create, read, update, and delete users in the
 * database using Hibernate ORM. It abstracts database interactions for the User
 * entity and allows querying users by ID or associated profile username.
 * </p>
 *
 * <pre>
 * {@code
 * // Example usage
 * User user = new User();
 * user.setEmail("example@email.com");
 * user.setPasswordHash("password123");
 * UserRepository.save(user);
 * User foundUser = UserRepository.findByUsername("username123");
 * }</pre>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-03
 * @see com.serinity.accesscontrol.model.User
 * @see com.serinity.accesscontrol.repository.ProfileRepository
 *
 *      <a
 *      href=
 *      "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/repository/UserRepository.java">
 *      UserRepository.java
 *      </a>
 */
public final class UserRepository extends BaseRepository<User, Long> {
  public UserRepository(final EntityManager em) {
    super(em, User.class);
  }

  public User findUserByEmail(final String email) {
    try {
      return em.createQuery(User.class)
          .where("email", email)
          .getSingleResult();
    } catch (final Exception e) {
      return null;
    }
  }

} // UserRepository final class
