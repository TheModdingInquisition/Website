package org.moddinginquisition.web.backend.db

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import org.jdbi.v3.core.Jdbi

@Canonical
@CompileStatic
class Database {
    Jdbi jdbi

    private final Map<Class, PojoDAO> daos = new HashMap<>()
    def <T> PojoDAO<T> get(Class<T> clazz) {
        return daos.computeIfAbsent(clazz, it -> new PojoDAOImpl<>(clazz, jdbi, clazz.getAnnotation(ForTable).value()))
    }
}
