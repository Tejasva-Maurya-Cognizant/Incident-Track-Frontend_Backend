package com.incidenttracker.backend.common.exception;

/**
 * Thrown when a request requires an authenticated user but none is present.
 * Typical cases: missing/invalid JWT, or no user found in the security context.
 */
public class AuthenticationRequiredException extends RuntimeException {
    public AuthenticationRequiredException(String message) {
        super(message);
    }
}
