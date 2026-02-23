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
  public AuthSessionRepository(final EntityManager em) {
    super(em, AuthSession.class);
  }

  public AuthSession findByRefreshToken(final String refreshToken) {
    try {
      return em.createQuery(AuthSession.class)
          .where("refresh_token", refreshToken)
          .getSingleResult();
    } catch (final Exception e) {
      return null;
    }
  }

  public List<AuthSession> findByUserId(final UUID userId) {
    try {
      return em.createQuery(AuthSession.class)
          .where("user_id", userId)
          .getResultList();
    } catch (final Exception e) {
      return null;
    }
  }

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

  public Optional<AuthSession> findActiveSession(final User user) {
    try {
      AuthSession session = em.createQuery(AuthSession.class)
          .where("user_id", user.getId())
          .where("revoked", false)
          .where("expires_at", ">", Instant.now())
          .getSingleResult();
      return Optional.ofNullable(session);

    } catch (final Exception e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

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
