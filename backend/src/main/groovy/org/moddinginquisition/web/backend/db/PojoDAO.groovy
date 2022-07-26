package org.moddinginquisition.web.backend.db

import groovy.transform.CompileStatic

@CompileStatic
interface PojoDAO<T> {
    List<T> getAll()

    SelectionQuery<T> select()

    void insert(T value)

    def <Z> Z insertReturning(T value, Closure<Z> toReturn)

    DeleteStatement<T> delete()

    UpdateStatement<T> update()
}