package org.moddinginquisition.web.backend.util

import groovy.transform.CompileStatic

@CompileStatic
enum ErrorResponder {
    INVALID_ID('invalid_id'),
    UNEXPECTED_MODIFICATION('unexpected_modification'),
    MISSING_FIELD('missing_field'),
    INVALID_SYNTAX('invalid_syntax'),

    FORBIDDEN('forbidden', 'This endpoint requires you to be a Janitor'),
    UNAUTHORIZED('unauthorized', 'No authorization token provided')
    ;

    private final String code, message

    ErrorResponder(String code, String message = null) {
        this.code = code
        this.message = message
    }

    String withMessage(String message) {
return """
    {
        "err": "$code",
        "message": "$message"
    }
"""
    }

    @Override
    String toString() {
return """
    {
        "err": "$code",
        "message": "$message"
    }
"""
    }
}
