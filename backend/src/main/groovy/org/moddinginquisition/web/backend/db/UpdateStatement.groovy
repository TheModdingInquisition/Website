package org.moddinginquisition.web.backend.db

import org.jetbrains.annotations.Nullable

interface UpdateStatement<T> {
    def <Z> UpdateStatement<T> where(Closure<Z> type, @Nullable Z value)
    def <Z> UpdateStatement<T> set(Closure<Z> type, @Nullable Z value)
    int execute()
}
