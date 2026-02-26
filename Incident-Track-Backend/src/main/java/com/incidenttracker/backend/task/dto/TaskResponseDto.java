package com.incidenttracker.backend.task.dto;

import java.time.LocalDateTime;

import com.incidenttracker.backend.common.enums.TaskStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskResponseDto {

    // Unique task id.
    private Long taskId;
    // Display title for the task.
    private String title;
    // Detailed task description.
    private String description;
    // Current task workflow status.
    private TaskStatus status;
    // Deadline by which task should be completed.
    private LocalDateTime dueDate;
    // Creation timestamp.
    private LocalDateTime createdDate;
    // Assignee user id.
    private Long assignedTo;
    // Assigner user id.
    private Long assignedBy;
    // Linked incident id.
    private Long incidentId;

}
