package com.incidenttracker.backend.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequestDto {

    // Required task title from API client.
    @NotBlank
    private String title;

    // Required task description from API client.
    @NotBlank
    private String description;

    // Target assignee user id.
    @NotNull
    private Long assignedTo; // Just the ID

    // Incident id to which this task belongs.
    @NotNull
    private Long incidentId; // Just the ID

}
