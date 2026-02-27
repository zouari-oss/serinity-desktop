// `AuthSessionRepository` package name
package com.serinity.accesscontrol.repository;

// `java` import(s)
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.core.EntityManager;

// `serinity` import(s)
import com.serinity.accesscontrol.model.AuthSession;
import com.serinity.accesscontrol.model.User;
import com.serinity.accesscontrol.repository.base.BaseRepository;

/**
 * Repository class for performing CRUD operations on
 * {@link com.serinity.accesscontrol.model.AuthSession} entities.
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-04
 * @see com.serinity.accesscontrol.model.AuthSession
 *
 *      <a
 *      href=
 *      "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/repository/AuthSessionRepository.java">
 *      AuthSessionRepository.java
 *      </a>
 */
public class AuthSessionRepository extends BaseRepository<AuthSession, Long> {
  private static final org.apache.logging.log4j.Logger _LOGGER = org.apache.logging.log4j.LogManager
      .getLogger(AuthSessionRepository.class);

  public AuthSessionRepository(final EntityManager em) {
    super(em, AuthSession.class);
  }

  /**
   * Finds an {@link AuthSession} by its refresh token value.
   *
   * @param refreshToken the refresh token to look up
   * @return the matching session, or {@code null} if not found
   */
  public AuthSession findByRefreshToken(final String refreshToken) {
    try {
      return em.createQuery(AuthSession.class)
          .where("refresh_token", refreshToken)
          .getSingleResult();
    } catch (final Exception e) {
      return null;
    }
  }

  /**
   * Returns all sessions belonging to a given user.
   *
   * @param userId the UUID of the user
   * @return list of sessions for the user, or {@code null} on error
   */
  public List<AuthSession> findByUserId(final UUID userId) {
    try {
      return em.createQuery(AuthSession.class)
          .where("user_id", userId)
          .getResultList();
    } catch (final Exception e) {
      return null;
    }
  }

  /**
   * Finds a valid (non-revoked, non-expired) session by refresh token.
   *
   * @param refreshToken the refresh token to validate
   * @return the active session, or {@code null} if none found
   */
  public AuthSession findValidByRefreshToken(final String refreshToken) {
    try {
      return em.createQuery(AuthSession.class)
          .where("refresh_token", refreshToken)
          .where("revoked", false)
          .where("expires_at", ">", Instant.now())
          .getSingleResult();
    } catch (final Exception e) {
      return null;
    }
  }

  /**
   * Finds the currently active (non-revoked, non-expired) session for a user.
   *
   * @param user the user whose active session to find
   * @return an {@link Optional} containing the active session, or empty if none
   */
  public Optional<AuthSession> findActiveSession(final User user) {
    try {
      final AuthSession session = em.createQuery(AuthSession.class)
          .where("user_id", user.getId())
          .where("revoked", false)
          .where("expires_at", ">", Instant.now())
          .getSingleResult();
      return Optional.ofNullable(session);

    } catch (final Exception e) {
      _LOGGER.warn("No active session found for user: {}", user.getId());
      return Optional.empty();
    }
  }

  /**
   * Checks whether the user has at least one active (non-revoked, non-expired)
   * session.
   *
   * @param user the user to check
   * @return {@code true} if an active session exists
   */
  public boolean existsActiveSession(final User user) {
    try {
      return em.createQuery(AuthSession.class)
          .where("user_id", user.getId())
          .where("revoked", false)
          .where("expires_at", ">", Instant.now())
          .count() > 0;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
} // AuthSessionRepository final class
