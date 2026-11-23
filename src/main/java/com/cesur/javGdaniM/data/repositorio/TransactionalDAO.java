package com.cesur.javGdaniM.data.repositorio;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
public interface TransactionalDAO<T> {
    Optional<T> findById(Long id);
    Optional<T> findById(Connection conn, Long id);
    List<T> findAll();
    List<T> findAll(Connection conn);
    T save(T obj);
    T save(Connection conn, T obj);
    void update(T entity);
    void update(Connection conn, T entity);
    void deleteById(Long id);
    void deleteById(Connection conn, Long id);
}
