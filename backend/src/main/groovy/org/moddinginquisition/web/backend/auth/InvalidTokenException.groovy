package org.moddinginquisition.web.backend.auth

class InvalidTokenException extends AuthException {
    InvalidTokenException(String token) {
        super("Provided token ($token) was invalid!")
    }
}
