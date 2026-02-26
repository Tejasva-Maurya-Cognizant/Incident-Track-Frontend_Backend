package com.incidenttracker.backend.common.exception;

/**
 * Thrown when the client sends invalid input that cannot be processed.
 * Typical cases: invalid enum/status value, malformed request data, or failed validation.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
