package com.incidenttracker.backend.task.dto;

import jakarta.validation.constraints.NotBlank;

// Request payload for task status update endpoint.
// Record keeps input immutable and exposes accessor as status().
public record TaskStatusUpdateRequestDto(
        @NotBlank(message = "Status cannot be empty") String status) {
}
