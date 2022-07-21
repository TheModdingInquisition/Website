package org.moddinginquisition.web.backend.db

import groovy.transform.CompileStatic
import org.jdbi.v3.core.result.ResultIterable
import org.jetbrains.annotations.Nullable

import java.util.function.Function

@CompileStatic
interface SelectionQuery<T> {
    abstract <Z> SelectionQuery<T> and(Closure<Z> type, @Nullable Z value, Strategy strategy)
    default <Z> SelectionQuery<T> and(Closure<Z> type, @Nullable Z value) {
        return and(type, value, Strategy.EQUALS)
    }

    def <Z> Z execute(Function<ResultIterable<T>, Z> mapper)

    enum Strategy {
        EQUALS {
            @Override
            String toString() {
                return "="
            }
        },
        NOT_EQUALS {
            @Override
            String toString() {
                return "!="
            }
        }
    }
}