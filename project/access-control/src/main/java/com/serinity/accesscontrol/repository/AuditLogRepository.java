// `UserRepository` package name
package com.serinity.accesscontrol.repository;

// `java` import(s)
import java.util.List;
import java.util.UUID;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.core.EntityManager;

// `serinity` import(s)
import com.serinity.accesscontrol.model.AuditLog;
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
   * @return list of audit logs for the session, or {@code null} on error
   */
  public List<AuditLog> findByAuthSessionId(final UUID authSessionId) {
    try {
      return em.createQuery(AuditLog.class)
          .where("auth_session_id", authSessionId)
          .getResultList();
    } catch (final Exception e) {
      return null;
    }
  }
} // UserRepository final class
