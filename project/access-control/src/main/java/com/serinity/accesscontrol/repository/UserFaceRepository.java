// `UserFaceRepository` package name
package com.serinity.accesscontrol.repository;

// `java` import(s)
import java.util.UUID;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.core.EntityManager;

// `serinity` import(s)
import com.serinity.accesscontrol.model.UserFace;
import com.serinity.accesscontrol.repository.base.BaseRepository;

/**
 * Repository class for performing CRUD operations on
 * {@link com.serinity.accesscontrol.model.UserFace} entities.
 *
 * <p>
 * Provides methods to persist and retrieve face embeddings associated with
 * users, enabling face-recognition-based authentication flows.
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-03
 * @see com.serinity.accesscontrol.model.UserFace
 * @see com.serinity.accesscontrol.repository.UserRepository
 *
 *      <a
 *      href=
 *      "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/repository/UserFaceRepository.java">
 *      UserFaceRepository.java
 *      </a>
 */
public final class UserFaceRepository extends BaseRepository<UserFace, UUID> {

  public UserFaceRepository(final EntityManager em) {
    super(em, UserFace.class);
  }

  /**
   * Finds the face embedding associated with a given user.
   *
   * @param userId the UUID of the user
   * @return the {@link UserFace} for the user, or {@code null} if not found
   */
  public UserFace findByUserId(final UUID userId) {
    try {
      return em.createQuery(UserFace.class)
          .where("user_id", userId)
          .getSingleResult();
    } catch (final Exception e) {
      return null;
    }
  }
} // UserFaceRepository final class
