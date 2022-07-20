package org.moddinginquisition.web.backend.db

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import org.jdbi.v3.core.Jdbi

@Canonical
@CompileStatic
class Database {
    Jdbi jdbi
}
