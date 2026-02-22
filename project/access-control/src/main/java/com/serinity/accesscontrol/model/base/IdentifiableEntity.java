// `IdentifiableEntity` package name
package com.serinity.accesscontrol.model.base;

// `java` import(s)
import java.util.UUID;

// `jakarta` import(s)
import org.zouarioss.skinnedratorm.annotations.GeneratedValue;
import org.zouarioss.skinnedratorm.annotations.Id;
import org.zouarioss.skinnedratorm.annotations.MappedSuperclass;
import org.zouarioss.skinnedratorm.flag.GenerationType;

/**
 * Base class for entities that require a unique identifier. Provides a
 * universally unique identifier (UUID) as the primary key for all inheriting
 * entities.
 *
 * <p>
 * All entities extending this class automatically have a primary key field
 * named {@code id}, which is generated using the {@link GenerationType#UUID}
 * strategy.
 * </p>
 *
 * <p>
 * NOTE: This class is annotated with {@code @MappedSuperclass}, so it does not
 * correspond to a database table itself but allows its fields to be inherited
 * by child entity classes.
 * </p>
 *
 * @author @ZouariOmar <zouariomar20@gmail.com>
 * @version 1.0
 * @since 2026-02-03
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/model/base/IdentifiableEntity.java">
 *        IdentifiableEntity.java
 *        </a>
 */
@MappedSuperclass
public abstract class IdentifiableEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  protected UUID id;

  public UUID getId() {
    return id;
  }
} // IdentifiableEntity abstract class
