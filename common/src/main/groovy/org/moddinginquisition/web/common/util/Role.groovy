package org.moddinginquisition.web.common.util

import io.javalin.core.security.RouteRole

enum Role implements RouteRole {
    ANYONE,
    JANITOR
}