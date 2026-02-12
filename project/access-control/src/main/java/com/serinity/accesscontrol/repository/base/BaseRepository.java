/**
 * BaseRepository.java
 *
 * A base repository that support the common crud actions
 *
 * <p>none</p>
 *
 * @author  @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since   2026-02-04
 *
 * <a
 * href="https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/repository/base/BaseRepository.java"
 * target="_blank">
 * BaseRepository.java
 * </a>
 */

// `serinity` package name
package com.serinity.accesscontrol.repository.base;

// `java` import(s)
import java.util.List;
import java.util.Optional;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.core.EntityManager;
import org.zouarioss.skinnedratorm.engine.QueryBuilder;

public abstract class BaseRepository<T, ID> {
  protected final EntityManager em;
  protected final Class<T> entityClass;

  public BaseRepository(final EntityManager em, final Class<T> entityClass) {
    this.em = em;
    this.entityClass = entityClass;
  }

  public void save(final T entity) {
    try {
      em.persist(entity);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Optional<T> findById(final ID id) {
    try {
      return Optional.ofNullable(em.findById(entityClass, id));
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public void delete(final T entity) {
    try {
      em.delete(entity);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void deleteById(final ID id) {
    findById(id).ifPresent(this::delete);
  }

  public List<T> findAll() {
    try {
      return new QueryBuilder<>(entityClass, em.getConnection())
          .getResultList();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
} // BaseRepository abstract class
