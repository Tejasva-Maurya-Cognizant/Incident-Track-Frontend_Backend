package com.incidenttracker.backend.common.exception;

/**
 * Thrown when the request conflicts with the current state of the resource.
 * Typical cases: invalid state transitions or actions that violate workflow rules.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
