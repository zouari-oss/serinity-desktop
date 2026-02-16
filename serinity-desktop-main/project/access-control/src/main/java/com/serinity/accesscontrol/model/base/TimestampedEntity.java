/**
 * TimestampedEntity.java
 *
 * Base class for entities that need automatic tracking of creation and update timestamps.
 * Extends {@link IdentifiableEntity} to inherit a UUID primary key.
 *
 * <p>This class is annotated with {@code @MappedSuperclass}, so it does not correspond
 * to a database table itself. Child entities will inherit the {@code createdAt} and
 * {@code updatedAt} fields, which are automatically managed by Hibernate.</p>
 *
 * <p>Fields:</p>
 * <ul>
 *   <li>{@code createdAt} – the timestamp when the entity was first persisted; immutable.</li>
 *   <li>{@code updatedAt} – the timestamp of the last update to the entity; automatically updated on modification.</li>
 * </ul>
 *
 * <p>Note: Hibernate annotations {@link org.zouarioss.skinnedratorm.annotations.CreationTimestamp} and
 * {@link org.zouarioss.skinnedratorm.annotations.UpdateTimestamp} are used to manage timestamps automatically.</p>
 *
 * @author  @ZouariOmar <zouariomar20@gmail.com>
 * @version 1.0
 * @since   2026-02-03
 *
 * <a
 * href="https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/model/base/TimestampedEntity.java"
 * target="_blank">
 * TimestampedEntity.java
 * </a>
 */

// `TimestampedEntity` pckage name
package com.serinity.accesscontrol.model.base;

// `java` import(s)
import java.time.Instant;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.annotations.Column;
import org.zouarioss.skinnedratorm.annotations.CreationTimestamp;
import org.zouarioss.skinnedratorm.annotations.MappedSuperclass;
import org.zouarioss.skinnedratorm.annotations.UpdateTimestamp;

@MappedSuperclass
public abstract class TimestampedEntity extends IdentifiableEntity {
  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  protected Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  protected Instant updatedAt;
} // TimestampedEntity abstract class
