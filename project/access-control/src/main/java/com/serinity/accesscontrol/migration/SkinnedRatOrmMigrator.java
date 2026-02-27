// `SkinnedRatOrmMigrator` package name
package com.serinity.accesscontrol.migration;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.flag.SQLDialect;
import org.zouarioss.skinnedratorm.engine.SchemaGenerator;

// `serinity` import(s)
import com.serinity.accesscontrol.config.SkinnedRatOrmEntityManager;
import com.serinity.accesscontrol.model.AuditLog;
import com.serinity.accesscontrol.model.AuthSession;
import com.serinity.accesscontrol.model.Profile;
import com.serinity.accesscontrol.model.User;
import com.serinity.accesscontrol.model.UserFace;

/**
 * Database schema migration utility for the Access Control module.
 *
 * <p>
 * This class is responsible for managing schema generation using
 * {@code skinned-rat-orm}. It performs a full migration cycle by dropping
 * existing tables and recreating them in the correct order based on
 * entity relationships.
 * </p>
 *
 * <p>
 * The migration targets the MySQL dialect and operates on the following
 * entities:
 * </p>
 * <ul>
 * <li>{@link com.serinity.accesscontrol.model.User}</li>
 * <li>{@link com.serinity.accesscontrol.model.AuthSession}</li>
 * <li>{@link com.serinity.accesscontrol.model.Profile}</li>
 * <li>{@link com.serinity.accesscontrol.model.AuditLog}</li>
 * </ul>
 *
 * <p>
 * WARN: This operation is destructive. All existing tables mapped to the
 * above entities will be permanently dropped before being recreated.
 * It is intended for development, testing, or controlled reset
 * scenarios, not for production environments unless explicitly required.
 * </p>
 *
 * <p>
 * The migration process relies on:
 * </p>
 * <ul>
 * <li>{@code SkinnedRatOrmEntityManager} for database connection handling</li>
 * <li>{@code SchemaGenerator} for DDL operations</li>
 * <li>{@code SQLDialect.MYSQL} as the configured SQL dialect</li>
 * </ul>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-15
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/migration/SkinnedRatOrmMigrator.java"
 *        target="_blank">
 *        SkinnedRatOrmMigrator.java
 *        </a>
 */
public final class SkinnedRatOrmMigrator {
  private static final SchemaGenerator generator = new SchemaGenerator(
      SkinnedRatOrmEntityManager.getConnection(),
      SQLDialect.MYSQL);

  /**
   * Runs the full schema migration: drops all managed tables then recreates
   * them in dependency order.
   *
   * <p>
   * <strong>WARN:</strong> This is a destructive operation. All existing data
   * in the managed tables will be permanently lost.
   * </p>
   *
   * @throws RuntimeException if any DDL operation fails
   */
  public static void migrate() {
    try {
      // Drop old tables
      generator.dropTable(AuditLog.class);
      generator.dropTable(Profile.class);
      generator.dropTable(AuthSession.class);
      generator.dropTable(User.class);
      generator.dropTable(UserFace.class);

      // Create tables
      generator.createTable(User.class);
      generator.createTable(AuthSession.class);
      generator.createTable(Profile.class);
      generator.createTable(AuditLog.class);
      generator.createTable(UserFace.class);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
} // SkinnedRatOrmEntityManager class
