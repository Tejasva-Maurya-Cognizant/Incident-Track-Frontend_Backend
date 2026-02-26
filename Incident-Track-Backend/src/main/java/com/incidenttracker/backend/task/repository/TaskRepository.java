package com.incidenttracker.backend.task.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.incidenttracker.backend.common.enums.TaskStatus;
import com.incidenttracker.backend.task.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Returns tasks for a specific incident id.
    List<Task> findByIncident_IncidentId(Long incidentId);

    // Returns tasks assigned to a specific user id.
    List<Task> findByAssignedTo_UserId(Long assignedTo);

    // Returns tasks created by a specific assigner id.
    List<Task> findByAssignedBy_UserId(Long assignedBy);

    // Returns tasks filtered by status.
    List<Task> findByStatus(TaskStatus status);

    // Returns completed tasks for the given incident ids (used by reporting).
    List<Task> findByIncident_IncidentIdInAndCompletedDateIsNotNull(List<Long> incidentIds);

    // ---- Pageable versions ----
    Page<Task> findAll(Pageable pageable);

    Page<Task> findByIncident_IncidentId(Long incidentId, Pageable pageable);

    Page<Task> findByAssignedTo_UserId(Long assignedTo, Pageable pageable);

    Page<Task> findByAssignedBy_UserId(Long assignedBy, Pageable pageable);

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
}
