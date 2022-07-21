package org.moddinginquisition.web.backend.db

import org.jetbrains.annotations.Nullable

interface DeleteStatement<T> {
    def <Z> DeleteStatement<T> and(Closure<Z> type, @Nullable Z value)
    int execute()
}
