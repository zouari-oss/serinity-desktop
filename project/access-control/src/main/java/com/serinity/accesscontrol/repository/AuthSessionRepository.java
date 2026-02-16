/**
 * AuthSessionRepository.java
 *
 * Repository class for performing CRUD operations on {@link com.serinity.accesscontrol.model.AuthSession} entities.
 *
 * @author  @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since   2026-02-04
 * @see     com.serinity.accesscontrol.model.AuthSession
 *
 * <a
 * href="https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/repository/AuthSessionRepository.java"
 * target="_blank">
 * AuthSessionRepository.java
 * </a>
 */

// `AuthSessionRepository` package name
package com.serinity.accesscontrol.repository;

// `java` import(s)
import java.time.Instant;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.core.EntityManager;

import com.serinity.accesscontrol.config.SkinnedRatOrmEntityManager;
// `serinity` import(s)
import com.serinity.accesscontrol.model.AuthSession;
import com.serinity.accesscontrol.model.User;
import com.serinity.accesscontrol.repository.base.BaseRepository;

public class AuthSessionRepository extends BaseRepository<AuthSession, Long> {
  private EntityManager em;

  public AuthSessionRepository() {
    super(SkinnedRatOrmEntityManager.getEntityManager(), AuthSession.class);
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

  public boolean existsActiveSession(final User user, final Instant now) {
    try {
      final long count = em.createQuery(AuthSession.class)
          .where("user_id", user.getId())
          .where("revoked", false)
          .where("expires_at", ">", now)
          .count();

      return count > 0;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
} // AuthSessionRepository final class
