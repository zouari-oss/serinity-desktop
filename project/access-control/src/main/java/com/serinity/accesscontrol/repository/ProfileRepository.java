
// `ProfileRepository` package name
package com.serinity.accesscontrol.repository;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.core.EntityManager;

// `serinity` import(s)
import com.serinity.accesscontrol.model.Profile;
import com.serinity.accesscontrol.repository.base.BaseRepository;

/**
 * Repository class for performing CRUD operations on
 * {@link com.serinity.accesscontrol.model.Profile} entities.
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-03
 * @see com.serinity.accesscontrol.model.Profile
 * @see com.serinity.accesscontrol.repository.UserRepository
 *
 *      <a
 *      href=
 *      "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/repository/ProfileRepository.java">
 *      ProfileRepository.java
 *      </a>
 */
public final class ProfileRepository extends BaseRepository<Profile, Long> {
  public ProfileRepository(final EntityManager em) {
    super(em, Profile.class);
  }

  public Profile findByUsername(final String username) {
    try {
      return em.createQuery(Profile.class)
          .where("username", username)
          .getSingleResult();

    } catch (final Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
} // ProfileRepository final class
