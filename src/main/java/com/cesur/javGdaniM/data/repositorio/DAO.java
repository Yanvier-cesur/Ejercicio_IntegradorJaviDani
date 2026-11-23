package com.cesur.javGdaniM.data.repositorio;

import java.util.List;
import java.util.Optional;

public interface DAO<T> {
        Optional<T> findById(Long id);
        List<T> findAll();
        T save(T obj);
        void update(T entity);
        void deleteById(Long id);
    }
