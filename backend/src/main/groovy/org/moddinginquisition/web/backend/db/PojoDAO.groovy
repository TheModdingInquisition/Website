package org.moddinginquisition.web.backend.db

import groovy.transform.CompileStatic

@CompileStatic
interface PojoDAO<T> {
    List<T> getAll()

    void insert(T value)
}