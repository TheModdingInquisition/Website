package org.moddinginquisition.web.backend.db

import groovy.transform.CompileStatic
import org.codehaus.groovy.runtime.MethodClosure
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import org.jetbrains.annotations.Nullable

import java.lang.reflect.Field
import java.sql.ResultSet
import java.sql.SQLException
import java.util.function.Function

@CompileStatic
class PojoDAOImpl<T> implements PojoDAO<T> {
    private final Class<T> clazz
    private final Jdbi jdbi
    private final String table
    private final List<MetaProperty> typeProps
    private final Map<String, Field> fields

    private final String insertValueTypes

    PojoDAOImpl(Class<T> clazz, Jdbi jdbi, String table) {
        this.clazz = clazz
        this.jdbi = jdbi
        this.table = table
        fields = new HashMap<>()
        for (f in clazz.getDeclaredFields())
            fields.put(f.name, f)
        this.typeProps = clazz.getMetaClass().getProperties()
            .stream().filter(it -> it.getName() != 'class' && !fields[it.name].isAnnotationPresent(IgnoreInTransaction.class))
            .toList()

        insertValueTypes = typeProps*.getName().join(",")

        final readProps = clazz.getMetaClass().getProperties()
            .stream().filter(it -> it.name != 'class').toList()
        final ctor = clazz.getDeclaredConstructor()
        jdbi.registerRowMapper(clazz, new RowMapper<T>() {
            @Override
            T map(ResultSet rs, StatementContext ctx) throws SQLException {
                final T val = ctor.newInstance()
                readProps.forEach(property -> {
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

    private String buildInsert(T value) {
        final builder = new StringBuilder()
        builder.append('insert into ').append(table).append(' (').append(insertValueTypes).append(') values (')
        builder.append(
                typeProps.collect {
                    return "'${it.getProperty(value)}'"
                }.join(',')
        )
        builder.append(')')
        return builder
    }

    @Override
    void insert(T value) {
        final var stm = buildInsert(value) + ';'
        jdbi.useHandle(handle -> handle.createUpdate(stm).execute())
    }

    @Override
    def <Z> Z insertReturning(T value, Closure<Z> toReturn) {
        if (toReturn !instanceof MethodClosure)
            throw new IllegalArgumentException('Only method closures can be used for query statements!')
        final mthClos = (MethodClosure) toReturn
        final typeName = mthClos.getMethod().replaceFirst('get', '').toLowerCase(Locale.ROOT)
        final stm = buildInsert(value) + ';'
        final type = fields[typeName].getType()
        return jdbi.withHandle(handle -> handle.createUpdate(stm)
            .executeAndReturnGeneratedKeys(typeName)
            .mapTo(type)
            .one()) as Z
    }

    @Override
    SelectionQuery<T> select() {
        return new Query()
    }

    @Override
    DeleteStatement<T> delete() {
        return new Delete()
    }

    @CompileStatic
    class Query implements SelectionQuery<T> {
        // language=Text
        @SuppressWarnings('SqlNoDataSourceInspection')
        String selectedTypes = "*"
        String where = null

        @Override
        def <Z> SelectionQuery<T> and(Closure<Z> type, Z value, Strategy strategy) {
            if (type !instanceof MethodClosure)
                throw new IllegalArgumentException('Only method closures can be used for query statements!')
            if (value === null)
                return this
            final mthClos = (MethodClosure) type
            final typeName = mthClos.getMethod().replaceFirst('get', '').toLowerCase(Locale.ROOT)
            final statement = "$typeName $strategy '$value'"
            if (where) {
                where += ' and ' + statement
            } else {
                where = ' where ' + statement
            }
            return this
        }

        @Override
        Object execute(Function mapper) {
            return jdbi.withHandle(ctx -> mapper.apply(ctx.createQuery("select $selectedTypes from ${PojoDAOImpl.this.table}${where ?: ''}")
                    .mapTo(PojoDAOImpl.this.clazz)))
        }
    }
    
    @CompileStatic
    class Delete implements DeleteStatement<T> {
        String where = null

        @Override
        def <Z> DeleteStatement<T> and(Closure<Z> type, @Nullable Z value) {
            if (type !instanceof MethodClosure)
                throw new IllegalArgumentException('Only method closures can be used for where statements!')
            if (value === null)
                return this
            final mthClos = (MethodClosure) type
            final typeName = mthClos.getMethod().replaceFirst('get', '').toLowerCase(Locale.ROOT)
            final statement = "$typeName = '$value'"
            if (where) {
                where += ' and ' + statement
            } else {
                where = ' where ' + statement
            }
            return this
        }

        @Override
        int execute() {
            jdbi.withHandle(ctx -> ctx.createUpdate("delete from ${PojoDAOImpl.this.table}${where ?: ''}").execute())
        }
    }
}
