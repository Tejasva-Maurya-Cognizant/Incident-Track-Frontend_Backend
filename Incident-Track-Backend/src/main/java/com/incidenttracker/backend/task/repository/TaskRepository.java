package com.incidenttracker.backend.task.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.incidenttracker.backend.common.enums.TaskStatus;
import com.incidenttracker.backend.task.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

    boolean existsByIncident_IncidentId(Long incidentId);

    Optional<Task> findByTaskIdAndIncident_Category_Department_DepartmentId(Long taskId, Long departmentId);

    // Returns the task linked to a specific incident id.
    Optional<Task> findByIncident_IncidentId(Long incidentId);

    Optional<Task> findByIncident_IncidentIdAndIncident_Category_Department_DepartmentId(Long incidentId, Long departmentId);

    List<Task> findByIncident_Category_Department_DepartmentId(Long departmentId);

    // Returns tasks assigned to a specific user id.
    List<Task> findByAssignedTo_UserId(Long assignedTo);

    List<Task> findByAssignedTo_UserIdAndIncident_Category_Department_DepartmentId(Long assignedTo, Long departmentId);

    // Returns tasks created by a specific assigner id.
    List<Task> findByAssignedBy_UserId(Long assignedBy);

    List<Task> findByAssignedBy_UserIdAndIncident_Category_Department_DepartmentId(Long assignedBy, Long departmentId);

    // Returns tasks filtered by status.
    List<Task> findByStatus(TaskStatus status);

    List<Task> findByStatusAndIncident_Category_Department_DepartmentId(TaskStatus status, Long departmentId);

    // Returns completed tasks for the given incident ids (used by reporting).
    List<Task> findByIncident_IncidentIdInAndCompletedDateIsNotNull(List<Long> incidentIds);

    // ---- Pageable versions ----
    Page<Task> findAll(Pageable pageable);

    Page<Task> findByIncident_IncidentId(Long incidentId, Pageable pageable);

    Page<Task> findByIncident_IncidentIdAndIncident_Category_Department_DepartmentId(
            Long incidentId, Long departmentId, Pageable pageable);

    Page<Task> findByIncident_Category_Department_DepartmentId(Long departmentId, Pageable pageable);

    Page<Task> findByAssignedTo_UserId(Long assignedTo, Pageable pageable);

    Page<Task> findByAssignedTo_UserIdAndIncident_Category_Department_DepartmentId(
            Long assignedTo, Long departmentId, Pageable pageable);

    Page<Task> findByAssignedBy_UserId(Long assignedBy, Pageable pageable);

    Page<Task> findByAssignedBy_UserIdAndIncident_Category_Department_DepartmentId(
            Long assignedBy, Long departmentId, Pageable pageable);

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByStatusAndIncident_Category_Department_DepartmentId(
            TaskStatus status, Long departmentId, Pageable pageable);
}
