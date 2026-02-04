package com.serinity.accesscontrol.repository.base;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;

public abstract class BaseRepository<T, ID> {
  private final EntityManager em;
  private final Class<T> entityClass;

  public BaseRepository(final EntityManager em, final Class<T> entityClass) {
    this.em = em;
    this.entityClass = entityClass;
  }

  public void save(final T entity) {
    em.persist(entity);
  }

  public Optional<T> findById(final ID id) {
    return Optional.ofNullable(em.find(entityClass, id));
  }

  public void delete(final T entity) {
    em.remove(entity);
  }

  public void deleteById(final ID id) {
    findById(id).ifPresent(this::delete);
  }

  public List<T> findAll() {
    return em.createQuery(
        "SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass)
        .getResultList();
  }
} // BaseRepository abstract class
