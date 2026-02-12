package com.serinity.accesscontrol.migration;

import org.zouarioss.skinnedratorm.engine.SQLDialect;
import org.zouarioss.skinnedratorm.engine.SchemaGenerator;

import com.serinity.accesscontrol.config.SkinnedRatOrmEntityManager;
import com.serinity.accesscontrol.model.AuditLog;
import com.serinity.accesscontrol.model.AuthSession;
import com.serinity.accesscontrol.model.Profile;
import com.serinity.accesscontrol.model.User;

public final class SkinnedRatOrmMigrator {
  public static void migrate() throws Exception {
    SchemaGenerator generator = new SchemaGenerator(SkinnedRatOrmEntityManager.getConnection(), SQLDialect.MYSQL);

    // Drop old tables
    generator.dropTable(User.class);
    generator.dropTable(Profile.class);
    generator.dropTable(AuditLog.class);
    generator.dropTable(AuthSession.class);

    // Create tables
    generator.createTable(User.class);
    generator.createTable(AuthSession.class);
    generator.createTable(Profile.class);
    generator.createTable(AuditLog.class);
  }
} // SkinnedRatOrmEntityManager class
