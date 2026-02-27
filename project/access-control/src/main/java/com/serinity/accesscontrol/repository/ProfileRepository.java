// `ProfileRepository` package name
package com.serinity.accesscontrol.repository;

import java.util.UUID;

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
  private static final org.apache.logging.log4j.Logger _LOGGER = org.apache.logging.log4j.LogManager
      .getLogger(ProfileRepository.class);
  public ProfileRepository(final EntityManager em) {
    super(em, Profile.class);
  }

  /**
   * Finds a profile by its unique generated username.
   *
   * @param username the auto-generated username
   * @return the matching {@link Profile}
   * @throws RuntimeException if the profile is not found or a DB error occurs
   */
  public Profile findByUsername(final String username) {
    try {
      return em.createQuery(Profile.class)
          .where("username", username)
          .getSingleResult();

    } catch (final Exception e) {
      _LOGGER.error("Failed to find profile by username: {}", username, e);
      throw new RuntimeException(e);
   *
   * @param userId the UUID of the owner {@link com.serinity.accesscontrol.model.User}
   * @return the matching {@link Profile}
   * @throws RuntimeException if the profile is not found or a DB error occurs
   */
  public Profile findByUserId(final UUID userId) {
    try {
      return em.createQuery(Profile.class)
          .where("user_id", userId)
          .getSingleResult();
    } catch (final Exception e) {
      _LOGGER.error("Failed to find profile by userId: {}", userId, e);
      throw new RuntimeException(e);
