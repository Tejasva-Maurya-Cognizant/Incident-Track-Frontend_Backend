package com.incidenttracker.backend.task.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.incidenttracker.backend.audit_v1.repository.IncidentSlaBreachRepository;
import com.incidenttracker.backend.audit_v1.service.AuditService;
import com.incidenttracker.backend.common.dto.PagedResponse;
import com.incidenttracker.backend.common.enums.ActionType;
import com.incidenttracker.backend.common.enums.BreachStatus;
import com.incidenttracker.backend.common.enums.IncidentStatus;
import com.incidenttracker.backend.common.enums.TaskStatus;
import com.incidenttracker.backend.common.enums.UserRole;
import com.incidenttracker.backend.common.exception.AuthenticationRequiredException;
import com.incidenttracker.backend.common.exception.BadRequestException;
import com.incidenttracker.backend.common.exception.ConflictException;
import com.incidenttracker.backend.common.exception.ForbiddenException;
import com.incidenttracker.backend.common.exception.ResourceNotFoundException;
import com.incidenttracker.backend.common.security.SecurityService;
import com.incidenttracker.backend.incident.entity.Incident;
import com.incidenttracker.backend.incident.repository.IncidentRepository;
import com.incidenttracker.backend.notification.service.NotificationService;
import com.incidenttracker.backend.task.dto.TaskRequestDto;
import com.incidenttracker.backend.task.dto.TaskResponseDto;
import com.incidenttracker.backend.task.entity.Task;
import com.incidenttracker.backend.task.repository.TaskRepository;
import com.incidenttracker.backend.task.service.TaskService;
import com.incidenttracker.backend.user.entity.User;
import com.incidenttracker.backend.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

        private final TaskRepository taskRepository;
        private final IncidentRepository incidentRepository;
        private final UserRepository userRepository;
        private final SecurityService securityService;
        private final AuditService auditService;
        private final IncidentSlaBreachRepository breachRepository;
        private final NotificationService notificationService;

        @Override
        // Creates a task, links it to incident/user, and records audit entries.
        // Incident status is moved to IN_PROGRESS in entity @PrePersist.
        @Transactional // Good practice for creation logic
        public TaskResponseDto createTask(TaskRequestDto request) {

                Incident incident = incidentRepository.findById(request.getIncidentId())
                                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));

                User assignedTo = userRepository.findById(request.getAssignedTo())
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                User currentUser = securityService.getCurrentUser()
                                .orElseThrow(() -> new AuthenticationRequiredException("Authentication required"));

                Task task = Task.builder().title(request.getTitle())
                                .description(request.getDescription())
                                .assignedTo(assignedTo)
                                .assignedBy(currentUser)
                                .dueDate(incident.getSlaDueAt())
                                .incident(incident)
                                .build();

                Task saved = taskRepository.save(task);

                // ✅ Audit log: task created + assigned
                auditService.log(
                                incident,
                                currentUser,
                                ActionType.TASK_CREATED,
                                "TaskId=" + saved.getTaskId()
                                                + ", title=" + saved.getTitle()
                                                + ", assignedTo=" + assignedTo.getUserId()
                                                + ", dueDate=" + saved.getDueDate());

                // ✅ Audit log: incident moved to IN_PROGRESS (because task creation triggers
                // it)
                auditService.log(
                                incident,
                                currentUser,
                                ActionType.INCIDENT_STATUS_CHANGED,
                                "Incident moved to IN_PROGRESS because a task was created (TaskId=" + saved.getTaskId()
                                                + ")");

                notificationService.notifyEmployee(saved);

                return mapToResponse(saved);

        }

        @Override
        // Returns all tasks mapped to response DTOs.
        public List<TaskResponseDto> getAllTasks() {
                return taskRepository.findAll()
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        // Returns one task by id or throws not-found.
        public TaskResponseDto getTaskByTaskId(Long taskId) {
                Task task = taskRepository.findById(taskId)
                                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

                return mapToResponse(task);
        }

        @Override
        // Returns tasks associated with a given incident id.
        public List<TaskResponseDto> getTaskByIncidentId(Long incidentId) {

                return taskRepository.findByIncident_IncidentId(incidentId)
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        // Returns tasks assigned to the provided user id.
        public List<TaskResponseDto> getTaskByAssignedTo(Long assignedTo) {

                return taskRepository.findByAssignedTo_UserId(assignedTo)
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        // Returns tasks assigned to the currently authenticated user.
        public List<TaskResponseDto> getTaskAssigenedToMe() {
                User currentUser = securityService.getCurrentUser()
                                .orElseThrow(() -> new AuthenticationRequiredException("Authentication required"));

                return taskRepository.findByAssignedTo_UserId(currentUser.getUserId())
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        // Returns tasks created by the provided assigner id.
        public List<TaskResponseDto> getTaskByAssignedBy(Long assignedBy) {
                return taskRepository.findByAssignedBy_UserId(assignedBy)
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        // Returns tasks created by the current user.
        public List<TaskResponseDto> getTaskByAssignedByMe() {
                User currentUser = securityService.getCurrentUser()
                                .orElseThrow(() -> new AuthenticationRequiredException("Authentication required"));
                return taskRepository.findByAssignedBy_UserId(currentUser.getUserId())
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        // Returns tasks filtered by status.
        public List<TaskResponseDto> getTasktByStatus(TaskStatus status) {
                return taskRepository.findByStatus(status)
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        // Enforces role-based workflow transitions:
        // MANAGER can move PENDING->IN_PROGRESS, EMPLOYEE can move
        // IN_PROGRESS->COMPLETED.
        // On completion, incident may be auto-resolved if all tasks are completed.
        @Transactional
        public void updateTaskStatus(Long taskId, String status) { // userId → who is trying to change
                                                                   // status

                Task task = taskRepository.findById(taskId)
                                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

                // User user = userRepository.findById(userId)
                // .orElseThrow(() -> new ResourceNotFoundException("User not found: " +
                // userId));
                User currentUser = securityService.getCurrentUser()
                                .orElseThrow(() -> new AuthenticationRequiredException("Authentication required"));

                TaskStatus newStatus;

                try {
                        newStatus = TaskStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException ex) {
                        throw new BadRequestException("Invalid task status: " + status);
                }

                TaskStatus currentStatus = task.getStatus();

                // workflow rule for changing status
                switch (newStatus) {
                        case IN_PROGRESS -> {
                                if (currentStatus != TaskStatus.PENDING) {
                                        throw new ConflictException("Only PENDING tasks can move to IN_PROGRESS");
                                }
                                if (currentUser.getRole() != UserRole.MANAGER || task.getAssignedBy() == null
                                                || !task.getAssignedBy().getUserId()
                                                                .equals(currentUser.getUserId())) {
                                        throw new ForbiddenException(
                                                        "Only the assigned MANAGER can move task to IN_PROGRESS");
                                }
                        }
                        case COMPLETED -> {
                                if (currentStatus != TaskStatus.IN_PROGRESS) {
                                        throw new ConflictException("Only IN_PROGRESS tasks can be COMPLETED");
                                }
                                if (currentUser.getRole() != UserRole.EMPLOYEE
                                                || !task.getAssignedTo().getUserId().equals(currentUser.getUserId())) {
                                        throw new ForbiddenException("Only the assigned EMPLOYEE can COMPLETE task");
                                }
                        }
                        default -> throw new ConflictException("Invalid status transition");
                }

                TaskStatus oldStatus = task.getStatus();
                task.setStatus(newStatus);
                Task savedTask = taskRepository.save(task);

                // ✅ Audit log for task status change
                auditService.log(
                                savedTask.getIncident(),
                                currentUser,
                                ActionType.TASK_STATUS_CHANGED,
                                "TaskId=" + savedTask.getTaskId() + " status: " + oldStatus + " -> " + newStatus);

                if (newStatus == TaskStatus.COMPLETED) {
                        task.setCompletedDate(LocalDateTime.now());
                }
                // ✅ If task completed, maybe resolve incident
                if (newStatus == TaskStatus.COMPLETED) {

                        Long incidentId = savedTask.getIncident().getIncidentId();

                        if (allTasksCompletedForIncident(incidentId)) {

                                Incident incident = savedTask.getIncident();
                                IncidentStatus oldIncidentStatus = incident.getStatus();

                                // choose your final incident status here:
                                incident.setStatus(IncidentStatus.RESOLVED);
                                Incident savedIncident = incidentRepository.save(incident);
                                notificationService.notifyReporterIncidentResolved(savedIncident);

                                auditService.log(
                                                savedIncident,
                                                currentUser,
                                                ActionType.INCIDENT_STATUS_CHANGED,
                                                "Incident status: " + oldIncidentStatus
                                                                + " -> RESOLVED (All tasks completed)");

                                // ✅ Close breach record if exists
                                breachRepository.findByIncident_IncidentId(incidentId).ifPresent(breach -> {
                                        breach.setBreachStatus(BreachStatus.RESOLVED);
                                        breachRepository.save(breach);

                                        auditService.log(
                                                        savedIncident,
                                                        currentUser,
                                                        ActionType.INCIDENT_UPDATED,
                                                        "Breach closed because incident was RESOLVED");
                                });
                        }
                }

        }

        // Utility: checks whether every task of an incident is completed.
        private boolean allTasksCompletedForIncident(Long incidentId) {
                return taskRepository.findByIncident_IncidentId(incidentId)
                                .stream()
                                .allMatch(t -> t.getStatus() == TaskStatus.COMPLETED);
        }

        // ---- Paginated implementations ----

        @Override
        public PagedResponse<TaskResponseDto> getAllTasksPaged(Pageable pageable) {
                Page<Task> page = taskRepository.findAll(pageable);
                return toPagedResponse(page);
        }

        @Override
        public PagedResponse<TaskResponseDto> getTaskByIncidentIdPaged(Long incidentId, Pageable pageable) {
                Page<Task> page = taskRepository.findByIncident_IncidentId(incidentId, pageable);
                return toPagedResponse(page);
        }

        @Override
        public PagedResponse<TaskResponseDto> getTaskByAssignedToPaged(Long assignedTo, Pageable pageable) {
                Page<Task> page = taskRepository.findByAssignedTo_UserId(assignedTo, pageable);
                return toPagedResponse(page);
        }

        @Override
        public PagedResponse<TaskResponseDto> getTaskAssignedToMePaged(Pageable pageable) {
                User currentUser = securityService.getCurrentUser()
                                .orElseThrow(() -> new AuthenticationRequiredException("Authentication required"));
                Page<Task> page = taskRepository.findByAssignedTo_UserId(currentUser.getUserId(), pageable);
                return toPagedResponse(page);
        }

        @Override
        public PagedResponse<TaskResponseDto> getTaskByAssignedByPaged(Long assignedBy, Pageable pageable) {
                Page<Task> page = taskRepository.findByAssignedBy_UserId(assignedBy, pageable);
                return toPagedResponse(page);
        }

        @Override
        public PagedResponse<TaskResponseDto> getTaskByAssignedByMePaged(Pageable pageable) {
                User currentUser = securityService.getCurrentUser()
                                .orElseThrow(() -> new AuthenticationRequiredException("Authentication required"));
                Page<Task> page = taskRepository.findByAssignedBy_UserId(currentUser.getUserId(), pageable);
                return toPagedResponse(page);
        }

        @Override
        public PagedResponse<TaskResponseDto> getTaskByStatusPaged(TaskStatus status, Pageable pageable) {
                Page<Task> page = taskRepository.findByStatus(status, pageable);
                return toPagedResponse(page);
        }

        private PagedResponse<TaskResponseDto> toPagedResponse(Page<Task> page) {
                return PagedResponse.<TaskResponseDto>builder()
                                .content(page.getContent().stream().map(this::mapToResponse).toList())
                                .page(page.getNumber())
                                .size(page.getSize())
                                .totalElements(page.getTotalElements())
                                .totalPages(page.getTotalPages())
                                .last(page.isLast())
                                .first(page.isFirst())
                                .build();
        }

        // Maps entity fields to response DTO shape used by API.
        private TaskResponseDto mapToResponse(Task task) {

                return TaskResponseDto.builder()
                                .taskId(task.getTaskId())
                                .createdDate(task.getCreatedDate())
                                .description(task.getDescription())
                                .title(task.getTitle())
                                .status(task.getStatus())
                                .assignedTo(task.getAssignedTo().getUserId())
                                .assignedBy(task.getAssignedBy().getUserId())
                                .incidentId(task.getIncident().getIncidentId())
                                .dueDate(task.getDueDate())
                                .build();
        }
}
