package org.moddinginquisition.web.backend.db

import groovy.transform.CompileStatic
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext

import java.sql.ResultSet
import java.sql.SQLException

@CompileStatic
class PojoDAOImpl<T> implements PojoDAO<T> {
    private final Class<T> clazz
    private final Jdbi jdbi
    private final String table
    private final List<MetaProperty> typeProps

    private final String insertValueTypes

    PojoDAOImpl(Class<T> clazz, Jdbi jdbi, String table) {
        this.clazz = clazz
        this.jdbi = jdbi
        this.table = table
        this.typeProps = clazz.getMetaClass().getProperties()
            .stream().filter(it -> it.getName() != 'class')
            .toList()

        insertValueTypes = typeProps*.getName().join(",")

        jdbi.registerRowMapper(clazz, new RowMapper<T>() {
            @Override
            T map(ResultSet rs, StatementContext ctx) throws SQLException {
                final T val = clazz.getDeclaredConstructor().newInstance()
                typeProps.forEach(property -> {
                    property.setProperty(val, rs.getObject(property.getName()))
                })
                return val
            }
        })
    }

    @Override
    List<T> getAll() {
        return jdbi.withHandle(t -> t.createQuery("select * from $table").mapTo(clazz).list())
    }

    @Override
    void insert(T value) {
        final builder = new StringBuilder()
        builder.append('insert into ').append(table).append(' (').append(insertValueTypes).append(') values (')
        builder.append(
                typeProps.collect {
                    return "'${it.getProperty(value)}'"
                }.join(',')
        )
        builder.append(');')
        jdbi.useHandle(handle -> handle.createUpdate(builder.toString()).execute())
    }
}
