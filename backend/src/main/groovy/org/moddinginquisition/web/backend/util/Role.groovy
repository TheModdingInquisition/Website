package org.moddinginquisition.web.backend.util

import io.javalin.core.security.RouteRole

enum Role implements RouteRole {
    ANYONE,
    JANITOR
}