package com.incidenttracker.backend.task.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.incidenttracker.backend.common.dto.PagedResponse;
import com.incidenttracker.backend.common.enums.TaskStatus;
import com.incidenttracker.backend.task.dto.TaskRequestDto;
import com.incidenttracker.backend.task.dto.TaskResponseDto;

public interface TaskService {

    // Creates a new task for an incident and returns task details.
    public TaskResponseDto createTask(TaskRequestDto request);

    // Returns all tasks.
    public List<TaskResponseDto> getAllTasks();

    // Returns one task by task id.
    public TaskResponseDto getTaskByTaskId(Long taskId);

    // Returns tasks for a specific incident.
    public List<TaskResponseDto> getTaskByIncidentId(Long incidentId);

    // Returns tasks assigned to a specific user.
    public List<TaskResponseDto> getTaskByAssignedTo(Long assignedTo);

    // Returns tasks assigned to the logged-in user.
    public List<TaskResponseDto> getTaskAssigenedToMe();

    // Returns tasks created by a specific assigner.
    public List<TaskResponseDto> getTaskByAssignedBy(Long assignedBy);

    // Returns tasks created by the logged-in manager.
    public List<TaskResponseDto> getTaskByAssignedByMe();

    // Returns tasks by workflow status.
    public List<TaskResponseDto> getTasktByStatus(TaskStatus status);

    // Updates task status after validating transition and permissions.
    public void updateTaskStatus(Long taskId, String status);

    // ---- Paginated versions ----
    public PagedResponse<TaskResponseDto> getAllTasksPaged(Pageable pageable);

    public PagedResponse<TaskResponseDto> getTaskByIncidentIdPaged(Long incidentId, Pageable pageable);

    public PagedResponse<TaskResponseDto> getTaskByAssignedToPaged(Long assignedTo, Pageable pageable);

    public PagedResponse<TaskResponseDto> getTaskAssignedToMePaged(Pageable pageable);

    public PagedResponse<TaskResponseDto> getTaskByAssignedByPaged(Long assignedBy, Pageable pageable);

    public PagedResponse<TaskResponseDto> getTaskByAssignedByMePaged(Pageable pageable);

    public PagedResponse<TaskResponseDto> getTaskByStatusPaged(TaskStatus status, Pageable pageable);
}
