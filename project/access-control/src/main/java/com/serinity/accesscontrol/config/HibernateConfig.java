/**
 * HibernateConfig.java
 *
 * Utility class for configuring and accessing Hibernate's `SessionFactory`.
 *
 * <p>This class is responsible for configuring and providing a singleton instance of
 * Hibernate's {@link org.hibernate.SessionFactory}. It reads database connection
 * properties from a `.env` file (by default `.env.development`) using
 * {@link io.github.cdimascio.dotenv.Dotenv} and applies them to the Hibernate
 * configuration.</p>
 *
 * @author  @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since   2026-02-03
 *
 * <a
 * href="https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/config/HibernateConfig.java" 
 * target="_blank">
 * HibernateConfig.java
 * </a>
 */

// `HibernateConfig` package name
package com.serinity.accesscontrol.config;

// `hibernate` import(s)
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

// `serinity.accesscontrol` import(s)
import com.serinity.accesscontrol.flag.ResourceFile;

// `cdimascio` import(s)
import io.github.cdimascio.dotenv.Dotenv;

/**
 * Utility class for configuring and accessing Hibernate's `SessionFactory`.
 *
 * <p>
 * This class is responsible for configuring and providing a singleton instance
 * of Hibernate's {@link org.hibernate.SessionFactory}. It reads database
 * connection properties from a `.env` file (by default `.env.development`)
 * using {@link io.github.cdimascio.dotenv.Dotenv} and applies them to the
 * Hibernate configuration.
 * </p>
 *
 * <pre>
 * {@code
 * // Example usage
 * SessionFactory sessionFactory = HibernateConfig.getSessionFactory();
 * Session session = sessionFactory.openSession();
 * }</pre>
 */
public final class HibernateConfig {
  private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

  public static SessionFactory getSessionFactory() {
    return SESSION_FACTORY;
  }

  private static SessionFactory buildSessionFactory() {
    try {
      final Dotenv dotenv = Dotenv.configure()
          .filename(".env.development")
          .ignoreIfMissing()
          .load();

      final Configuration cfg = new Configuration();
      cfg.setProperty("hibernate.connection.url", dotenv.get("DATABASE_URL"));
      cfg.setProperty("hibernate.connection.username", dotenv.get("DATABASE_USERNAME"));
      cfg.setProperty("hibernate.connection.password", dotenv.get("DATABASE_PASSWORD"));
      cfg.setProperty("hibernate.dialect", dotenv.get("HIBERNATE_DIALECT"));
      cfg.configure(ResourceFile.HIBERNATE_CFG_FXML.getFileName());

      return cfg.buildSessionFactory();

    } catch (final Exception e) {
      e.printStackTrace();
      throw new ExceptionInInitializerError(e);
    }
  }
} // HibernateConfig final class
