/**
 * ProfileRepository.java
 *
 * Repository class for performing CRUD operations on {@link com.serinity.accesscontrol.model.Profile} entities.
 *
 * @author  @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since   2026-02-03
 * @see     com.serinity.accesscontrol.model.Profile
 * @see     com.serinity.accesscontrol.repository.UserRepository
 *
 * <a
 * href="https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/repository/ProfileRepository.java"
 * target="_blank">
 * ProfileRepository.java
 * </a>
 */

// `ProfileRepository` package name
package com.serinity.accesscontrol.repository;

// `hibernate` import(s)
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

// `serinity` import(s)
import com.serinity.accesscontrol.model.Profile;

public final class ProfileRepository {
  private static final SessionFactory sessionFactory = com.serinity.accesscontrol.config.HibernateConfig
      .getSessionFactory();

  public static Profile findByUsername(final String username) {
    try (Session session = sessionFactory.openSession()) {
      final String hql = "FROM Profile p WHERE p.username = :username";
      final Query<Profile> query = session.createQuery(hql, Profile.class);
      query.setParameter("username", username);
      return query.uniqueResult();
    }
  }

  public static void save(final Profile profile) {
    try (Session session = sessionFactory.openSession()) {
      final var tx = session.beginTransaction();
      session.persist(profile);
      tx.commit();
    }
  }

  public static void delete(final Profile profile) {
    try (Session session = sessionFactory.openSession()) {
      final var tx = session.beginTransaction();
      session.remove(profile);
      tx.commit();
    }
  }
} // ProfileRepository final class
