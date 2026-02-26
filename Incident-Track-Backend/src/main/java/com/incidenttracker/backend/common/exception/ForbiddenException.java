package com.incidenttracker.backend.common.exception;

/**
 * Thrown when an authenticated user is not allowed to perform the requested action.
 * Typical cases: role/ownership checks fail or the user lacks required permissions.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
