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

        private User getCurrentUserRequired() {
                return securityService.getCurrentUser()
                                .orElseThrow(() -> new AuthenticationRequiredException("Authentication required"));
        }

        private boolean isAdmin(User user) {
                return user.getRole() == UserRole.ADMIN;
        }

        private boolean isManager(User user) {
                return user.getRole() == UserRole.MANAGER;
        }

        private Long getDepartmentIdRequired(User user) {
                if (user.getDepartment() == null || user.getDepartment().getDepartmentId() == null) {
                        throw new ConflictException("Current user is not mapped to a department.");
                }
                return user.getDepartment().getDepartmentId();
        }

        private Long getIncidentDepartmentId(Incident incident) {
                if (incident.getCategory() == null || incident.getCategory().getDepartment() == null
                                || incident.getCategory().getDepartment().getDepartmentId() == null) {
                        throw new ConflictException("Incident is not mapped to a department.");
                }
                return incident.getCategory().getDepartment().getDepartmentId();
        }

        private Task getTaskVisibleToPrivilegedUser(Long taskId, User currentUser) {
                if (isManager(currentUser)) {
                        return taskRepository.findByTaskIdAndIncident_Category_Department_DepartmentId(
                                        taskId, getDepartmentIdRequired(currentUser))
                                        .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
                }
                if (isAdmin(currentUser)) {
                        return taskRepository.findById(taskId)
                                        .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
                }
                throw new ForbiddenException("Only admins and managers can access this task.");
        }

        @Override
        @Transactional
        public TaskResponseDto createTask(TaskRequestDto request) {
                User currentUser = getCurrentUserRequired();
                if (!isManager(currentUser)) {
                        throw new ForbiddenException("Only managers can create tasks.");
                }

                Long managerDepartmentId = getDepartmentIdRequired(currentUser);

                Incident incident = incidentRepository.findById(request.getIncidentId())
                                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));

                if (!managerDepartmentId.equals(getIncidentDepartmentId(incident))) {
                        throw new ForbiddenException("You can only create tasks for incidents in your department.");
                }

                if (incident.getStatus() == IncidentStatus.RESOLVED || incident.getStatus() == IncidentStatus.CANCELLED) {
                        throw new ConflictException("Cannot create a task for a closed incident.");
                }

                if (incident.getStatus() != IncidentStatus.OPEN) {
                        throw new ConflictException("A task can only be created when the incident is OPEN.");
                }

                if (taskRepository.existsByIncident_IncidentId(incident.getIncidentId())) {
                        throw new ConflictException("This incident already has a task assigned.");
                }

                User assignedTo = userRepository.findById(request.getAssignedTo())
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if (assignedTo.getRole() != UserRole.EMPLOYEE) {
                        throw new BadRequestException("Tasks can only be assigned to employees.");
                }

                if (!managerDepartmentId.equals(getDepartmentIdRequired(assignedTo))) {
                        throw new ForbiddenException("Assignee must belong to the same department.");
                }

                incident.setStatus(IncidentStatus.IN_PROGRESS);
                Incident savedIncident = incidentRepository.save(incident);

                Task task = Task.builder()
                                .title(request.getTitle())
                                .description(request.getDescription())
                                .assignedTo(assignedTo)
                                .assignedBy(currentUser)
                                .dueDate(savedIncident.getSlaDueAt())
                                .incident(savedIncident)
                                .status(TaskStatus.PENDING)
                                .build();

                Task saved = taskRepository.save(task);

                auditService.log(
                                incident,
                                currentUser,
                                ActionType.TASK_CREATED,
                                "TaskId=" + saved.getTaskId()
                                                + ", title=" + saved.getTitle()
                                                + ", assignedTo=" + assignedTo.getUserId()
                                                + ", dueDate=" + saved.getDueDate());

                auditService.log(
                                incident,
                                currentUser,
                                ActionType.INCIDENT_STATUS_CHANGED,
                                "Incident moved to IN_PROGRESS because TaskId=" + saved.getTaskId() + " was created");

                notificationService.notifyEmployee(saved);

                return mapToResponse(saved);
        }

        @Override
        public List<TaskResponseDto> getAllTasks() {
                User currentUser = getCurrentUserRequired();
                List<Task> tasks;
                if (isManager(currentUser)) {
                        tasks = taskRepository.findByIncident_Category_Department_DepartmentId(
                                        getDepartmentIdRequired(currentUser));
                } else if (isAdmin(currentUser)) {
                        tasks = taskRepository.findAll();
                } else {
                        throw new ForbiddenException("Only admins and managers can view all tasks.");
                }

                return tasks.stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        public TaskResponseDto getTaskByTaskId(Long taskId) {
                User currentUser = getCurrentUserRequired();
                return mapToResponse(getTaskVisibleToPrivilegedUser(taskId, currentUser));
        }

        @Override
        public List<TaskResponseDto> getTaskByIncidentId(Long incidentId) {
                User currentUser = getCurrentUserRequired();
                List<Task> tasks;
                if (isManager(currentUser)) {
                        tasks = taskRepository.findByIncident_IncidentIdAndIncident_Category_Department_DepartmentId(
                                        incidentId, getDepartmentIdRequired(currentUser))
                                        .stream()
                                        .toList();
                } else if (isAdmin(currentUser)) {
                        tasks = taskRepository.findByIncident_IncidentId(incidentId)
                                        .stream()
                                        .toList();
                } else {
                        throw new ForbiddenException("Only admins and managers can view tasks by incident.");
                }

                return tasks.stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        public List<TaskResponseDto> getTaskByAssignedTo(Long assignedTo) {
                User currentUser = getCurrentUserRequired();
                List<Task> tasks;
                if (isManager(currentUser)) {
                        tasks = taskRepository.findByAssignedTo_UserIdAndIncident_Category_Department_DepartmentId(
                                        assignedTo, getDepartmentIdRequired(currentUser));
                } else if (isAdmin(currentUser)) {
                        tasks = taskRepository.findByAssignedTo_UserId(assignedTo);
                } else {
                        throw new ForbiddenException("Only admins and managers can view tasks by assignee.");
                }

                return tasks.stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        public List<TaskResponseDto> getTaskAssigenedToMe() {
                User currentUser = getCurrentUserRequired();
                return taskRepository.findByAssignedTo_UserId(currentUser.getUserId())
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        public List<TaskResponseDto> getTaskByAssignedBy(Long assignedBy) {
                User currentUser = getCurrentUserRequired();
                List<Task> tasks;
                if (isManager(currentUser)) {
                        tasks = taskRepository.findByAssignedBy_UserIdAndIncident_Category_Department_DepartmentId(
                                        assignedBy, getDepartmentIdRequired(currentUser));
                } else if (isAdmin(currentUser)) {
                        tasks = taskRepository.findByAssignedBy_UserId(assignedBy);
                } else {
                        throw new ForbiddenException("Only admins and managers can view tasks by assigner.");
                }

                return tasks.stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        public List<TaskResponseDto> getTaskByAssignedByMe() {
                User currentUser = getCurrentUserRequired();
                return taskRepository.findByAssignedBy_UserId(currentUser.getUserId())
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        public List<TaskResponseDto> getTasktByStatus(TaskStatus status) {
                User currentUser = getCurrentUserRequired();
                List<Task> tasks;
                if (isManager(currentUser)) {
                        tasks = taskRepository.findByStatusAndIncident_Category_Department_DepartmentId(
                                        status, getDepartmentIdRequired(currentUser));
                } else if (isAdmin(currentUser)) {
                        tasks = taskRepository.findByStatus(status);
                } else {
                        throw new ForbiddenException("Only admins and managers can view tasks by status.");
                }

                return tasks.stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        @Transactional
        public void updateTaskStatus(Long taskId, String status) {
                Task task = taskRepository.findById(taskId)
                                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

                User currentUser = getCurrentUserRequired();
                Incident incident = task.getIncident();

                if (incident.getStatus() == IncidentStatus.RESOLVED || incident.getStatus() == IncidentStatus.CANCELLED) {
                        throw new ConflictException("Cannot update a task for a closed incident.");
                }

                TaskStatus newStatus;
                try {
                        newStatus = TaskStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException ex) {
                        throw new BadRequestException("Invalid task status: " + status);
                }

                TaskStatus currentStatus = task.getStatus();
                if (currentStatus == newStatus) {
                        throw new ConflictException("Task is already " + newStatus + ".");
                }

                boolean allowedActor = false;
                if (isManager(currentUser)) {
                        allowedActor = task.getAssignedBy() != null
                                        && task.getAssignedBy().getUserId().equals(currentUser.getUserId())
                                        && getDepartmentIdRequired(currentUser).equals(getIncidentDepartmentId(incident));
                } else if (currentUser.getRole() == UserRole.EMPLOYEE) {
                        allowedActor = task.getAssignedTo().getUserId().equals(currentUser.getUserId());
                }

                if (!allowedActor) {
                        throw new ForbiddenException("Only the assigned employee or manager can update this task.");
                }

                switch (newStatus) {
                        case IN_PROGRESS -> {
                                if (currentStatus != TaskStatus.PENDING) {
                                        throw new ConflictException("Only PENDING tasks can move to IN_PROGRESS");
                                }
                        }
                        case COMPLETED -> {
                                if (currentStatus != TaskStatus.IN_PROGRESS) {
                                        throw new ConflictException("Only IN_PROGRESS tasks can be COMPLETED");
                                }
                        }
                        default -> throw new ConflictException("Invalid status transition");
                }

                TaskStatus oldStatus = task.getStatus();
                if (newStatus == TaskStatus.COMPLETED) {
                        task.setCompletedDate(LocalDateTime.now());
                }
                task.setStatus(newStatus);

                Task savedTask = taskRepository.save(task);

                auditService.log(
                                savedTask.getIncident(),
                                currentUser,
                                ActionType.TASK_STATUS_CHANGED,
                                "TaskId=" + savedTask.getTaskId() + " status: " + oldStatus + " -> " + newStatus);

                if (newStatus == TaskStatus.COMPLETED) {
                        IncidentStatus oldIncidentStatus = incident.getStatus();
                        incident.setStatus(IncidentStatus.RESOLVED);
                        incident.setResolvedDate(LocalDateTime.now());

                        Incident savedIncident = incidentRepository.save(incident);
                        notificationService.notifyReporterIncidentResolved(savedIncident);

                        auditService.log(
                                        savedIncident,
                                        currentUser,
                                        ActionType.INCIDENT_STATUS_CHANGED,
                                        "Incident status: " + oldIncidentStatus
                                                        + " -> RESOLVED (TaskId=" + savedTask.getTaskId() + " completed)");

                        breachRepository.findByIncident_IncidentId(savedIncident.getIncidentId()).ifPresent(breach -> {
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

        @Override
        public PagedResponse<TaskResponseDto> getAllTasksPaged(Pageable pageable) {
                User currentUser = getCurrentUserRequired();
                Page<Task> page;
                if (isManager(currentUser)) {
                        page = taskRepository.findByIncident_Category_Department_DepartmentId(
                                        getDepartmentIdRequired(currentUser), pageable);
                } else if (isAdmin(currentUser)) {
                        page = taskRepository.findAll(pageable);
                } else {
                        throw new ForbiddenException("Only admins and managers can view all tasks.");
                }
                return toPagedResponse(page);
        }

        @Override
        public PagedResponse<TaskResponseDto> getTaskByIncidentIdPaged(Long incidentId, Pageable pageable) {
                User currentUser = getCurrentUserRequired();
                Page<Task> page;
                if (isManager(currentUser)) {
                        page = taskRepository.findByIncident_IncidentIdAndIncident_Category_Department_DepartmentId(
                                        incidentId, getDepartmentIdRequired(currentUser), pageable);
                } else if (isAdmin(currentUser)) {
                        page = taskRepository.findByIncident_IncidentId(incidentId, pageable);
                } else {
                        throw new ForbiddenException("Only admins and managers can view tasks by incident.");
                }
                return toPagedResponse(page);
        }

        @Override
        public PagedResponse<TaskResponseDto> getTaskByAssignedToPaged(Long assignedTo, Pageable pageable) {
                User currentUser = getCurrentUserRequired();
                Page<Task> page;
                if (isManager(currentUser)) {
                        page = taskRepository.findByAssignedTo_UserIdAndIncident_Category_Department_DepartmentId(
                                        assignedTo, getDepartmentIdRequired(currentUser), pageable);
                } else if (isAdmin(currentUser)) {
                        page = taskRepository.findByAssignedTo_UserId(assignedTo, pageable);
                } else {
                        throw new ForbiddenException("Only admins and managers can view tasks by assignee.");
                }
                return toPagedResponse(page);
        }

        @Override
        public PagedResponse<TaskResponseDto> getTaskAssignedToMePaged(Pageable pageable) {
                User currentUser = getCurrentUserRequired();
                Page<Task> page = taskRepository.findByAssignedTo_UserId(currentUser.getUserId(), pageable);
                return toPagedResponse(page);
        }

        @Override
        public PagedResponse<TaskResponseDto> getTaskByAssignedByPaged(Long assignedBy, Pageable pageable) {
                User currentUser = getCurrentUserRequired();
                Page<Task> page;
                if (isManager(currentUser)) {
                        page = taskRepository.findByAssignedBy_UserIdAndIncident_Category_Department_DepartmentId(
                                        assignedBy, getDepartmentIdRequired(currentUser), pageable);
                } else if (isAdmin(currentUser)) {
                        page = taskRepository.findByAssignedBy_UserId(assignedBy, pageable);
                } else {
                        throw new ForbiddenException("Only admins and managers can view tasks by assigner.");
                }
                return toPagedResponse(page);
        }

        @Override
        public PagedResponse<TaskResponseDto> getTaskByAssignedByMePaged(Pageable pageable) {
                User currentUser = getCurrentUserRequired();
                Page<Task> page = taskRepository.findByAssignedBy_UserId(currentUser.getUserId(), pageable);
                return toPagedResponse(page);
        }

        @Override
        public PagedResponse<TaskResponseDto> getTaskByStatusPaged(TaskStatus status, Pageable pageable) {
                User currentUser = getCurrentUserRequired();
                Page<Task> page;
                if (isManager(currentUser)) {
                        page = taskRepository.findByStatusAndIncident_Category_Department_DepartmentId(
                                        status, getDepartmentIdRequired(currentUser), pageable);
                } else if (isAdmin(currentUser)) {
                        page = taskRepository.findByStatus(status, pageable);
                } else {
                        throw new ForbiddenException("Only admins and managers can view tasks by status.");
                }
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
