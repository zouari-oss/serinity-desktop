package com.serinity.accesscontrol.repository;

import java.time.Instant;

import com.serinity.accesscontrol.model.AuthSession;
import com.serinity.accesscontrol.model.User;
import com.serinity.accesscontrol.repository.base.BaseRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

public class AuthSessionRepository extends BaseRepository<AuthSession, Long> {
  private EntityManager em;

  public AuthSessionRepository(final EntityManager em) {
    super(em, AuthSession.class);
  }

  public AuthSession findByRefreshToken(final String refreshToken) {
    try {
      return em.createQuery(
          "SELECT s FROM AuthSession s WHERE s.refreshToken = :token",
          AuthSession.class)
          .setParameter("token", refreshToken)
          .getSingleResult();
    } catch (final NoResultException e) {
      return null;
    }
  }

  public AuthSession findValidByRefreshToken(final String refreshToken) {
    try {
      return em.createQuery(
          "SELECT s FROM AuthSession s " +
              "WHERE s.refreshToken = :token " +
              "AND s.revoked = false " +
              "AND s.expiresAt > :now",
          AuthSession.class)
          .setParameter("token", refreshToken)
          .setParameter("now", Instant.now())
          .getSingleResult();
    } catch (final NoResultException e) {
      return null;
    }
  }

  public boolean existsActiveSession(final User user, final Instant now) {
    return em.createQuery(
        "SELECT COUNT(s) FROM AuthSession s " +
            "WHERE s.user = :user " +
            "AND s.revoked = false " +
            "AND s.expiresAt > :now",
        Long.class)
        .setParameter("user", user)
        .setParameter("now", now)
        .getSingleResult() > 0;
  }
} // AuthSessionRepository final class
