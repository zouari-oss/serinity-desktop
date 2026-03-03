// `AuditLogRepository` package name
package com.serinity.accesscontrol.repository;

// `java` import(s)
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.core.EntityManager;

// `serinity` import(s)
import com.serinity.accesscontrol.model.AuditLog;
import com.serinity.accesscontrol.model.AuthSession;
import com.serinity.accesscontrol.repository.base.BaseRepository;

/**
 * Repository class for performing CRUD operations on
 * {@link com.serinity.accesscontrol.model.AuditLog} entities.
 *
 * <p>
 * This class provides methods to create, read, update, and delete audit logs
 * in the database using Hibernate ORM. It abstracts database interactions
 * for the AuditLog entity and allows querying audit records by session,
 * action type, or creation timestamp.
 * </p>
 *
 * <pre>
 * {@code
 * // Example usage
 * EntityManager em = SkinnedRatOrmEntityManager.getEntityManager();
 * AuditLogRepository auditRepo = new AuditLogRepository(em);
 *
 * AuditLog log = new AuditLog();
 * log.setAction("USER_LOGIN");
 * log.setSession(session);
 *
 * auditRepo.save(log);
 * }
 * </pre>
 *
 * <p>
 * Typical use cases:
 * </p>
 * <ul>
 * <li>Tracking authentication events (login, logout, sign-up)</li>
 * <li>Recording security-sensitive operations</li>
 * <li>Auditing user activity per session</li>
 * </ul>
 *
 * @author @ZouariOmar
 * @version 1.0
 * @since 2026-02-17
 *
 * @see com.serinity.accesscontrol.model.AuditLog
 * @see com.serinity.accesscontrol.model.AuthSession
 */
public final class AuditLogRepository extends BaseRepository<AuditLog, Long> {
  public AuditLogRepository(final EntityManager em) {
    super(em, AuditLog.class);
  }

  /**
   * Returns all audit log entries associated with a given session.
   *
   * @param authSessionId the UUID of the {@link com.serinity.accesscontrol.model.AuthSession}
   * @return list of audit logs for the session, or an empty list on error
   */
  public List<AuditLog> findByAuthSessionId(final UUID authSessionId) {
    try {
      return em.createQuery(AuditLog.class)
          .where("auth_session_id", authSessionId)
          .getResultList();
    } catch (final Exception e) {
      return Collections.emptyList();
    }
  }

  /**
   * Returns all audit log entries for every session belonging to the given user.
   *
   * <p>
   * Fetches all sessions for the user (1 query) then fetches all audit logs and
   * filters by session ID in memory (1 query), reducing N+1 queries down to 2.
   * </p>
   *
   * @param userId the UUID of the user
   * @return all audit logs across the user's sessions, or an empty list on error
   */
  public List<AuditLog> findAllByUserId(final UUID userId) {
    try {
      final List<AuthSession> sessions = em.createQuery(AuthSession.class)
          .where("user_id", userId)
          .getResultList();

      if (sessions.isEmpty()) {
        return Collections.emptyList();
      }

      final Set<UUID> sessionIds = sessions.stream()
          .map(AuthSession::getId)
          .collect(Collectors.toSet());

      return findAll().stream()
          .filter(log -> log.getSession() != null && sessionIds.contains(log.getSession().getId()))
          .collect(Collectors.toList());
    } catch (final Exception e) {
      return Collections.emptyList();
    }
  }
} // AuditLogRepository final class
