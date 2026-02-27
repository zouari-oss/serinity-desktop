// `serinity` package name
package com.serinity.accesscontrol.repository.base;

// `java` import(s)
import java.util.List;
import java.util.Optional;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.core.EntityManager;
import org.zouarioss.skinnedratorm.engine.QueryBuilder;

/**
 * A base repository that support the common crud actions
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-04
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/repository/base/BaseRepository.java">
 *        BaseRepository.java
 *        </a>
 */
public abstract class BaseRepository<T, ID> {
  private static final org.apache.logging.log4j.Logger _LOGGER = org.apache.logging.log4j.LogManager
      .getLogger(BaseRepository.class);
  protected final EntityManager em;
  protected final Class<T> entityClass;

  /**
   * Constructs a repository for the given entity class.
   *
   * @param em          the ORM {@link EntityManager} to use for DB operations
   * @param entityClass the entity class managed by this repository
   */
  public BaseRepository(final EntityManager em, final Class<T> entityClass) {
    this.em = em;
    this.entityClass = entityClass;
  }

  /**
   * Persists a new entity to the database.
   *
   * @param entity the entity to save
   */
  public void save(final T entity) {
    try {
      em.persist(entity);
    } catch (final Exception e) {
      _LOGGER.error("Failed to persist entity: {}", entity, e);
    }
   *
   * @param entity the entity with updated field values
   */
  public void update(final T entity) {
    try {
      em.update(entity);
    } catch (final Exception e) {
      _LOGGER.error("Failed to update entity: {}", entity, e);
    }
  }

  /**
   * Finds an entity by its primary key.
   *
   * @param id the primary key value
   * @return an {@link Optional} containing the entity, or empty if not found
   */
  public Optional<T> findById(final ID id) {
    try {
      return Optional.ofNullable(em.findById(entityClass, id));
    } catch (final Exception e) {
      _LOGGER.error("Failed to find entity by id: {}", id, e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Deletes an entity from the database.
   *
   * @param entity the entity instance to remove
   */
  public void delete(final T entity) {
    try {
      em.delete(entity);
    } catch (final Exception e) {
      _LOGGER.error("Failed to delete entity: {}", entity, e);
    }
  }

  /**
   * Deletes an entity identified by its primary key, if it exists.
   *
   * @param id the primary key of the entity to delete
   */
  public void deleteById(final ID id) {
    findById(id).ifPresent(this::delete);
  }

  /**
   * Returns all entities of this type from the database.
   *
   * @return a list of all persisted entities; never {@code null}
   */
  public List<T> findAll() {
    try {
      return new QueryBuilder<>(entityClass, em.getConnection())
          .getResultList();
    } catch (final Exception e) {
      _LOGGER.error("Failed to find all entities of type: {}", entityClass.getSimpleName(), e);
      throw new RuntimeException(e);
