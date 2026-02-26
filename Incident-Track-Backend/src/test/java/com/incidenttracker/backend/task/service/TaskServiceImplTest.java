package com.incidenttracker.backend.task.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.incidenttracker.backend.audit_v1.entity.IncidentSlaBreach;
import com.incidenttracker.backend.audit_v1.repository.IncidentSlaBreachRepository;
import com.incidenttracker.backend.audit_v1.service.AuditService;
import com.incidenttracker.backend.common.enums.ActionType;
import com.incidenttracker.backend.common.enums.BreachStatus;
import com.incidenttracker.backend.common.enums.IncidentStatus;
import com.incidenttracker.backend.common.enums.TaskStatus;
import com.incidenttracker.backend.common.enums.UserRole;
import com.incidenttracker.backend.common.exception.BadRequestException;
import com.incidenttracker.backend.common.security.SecurityService;
import com.incidenttracker.backend.incident.entity.Incident;
import com.incidenttracker.backend.incident.repository.IncidentRepository;
import com.incidenttracker.backend.notification.service.NotificationService;
import com.incidenttracker.backend.task.dto.TaskRequestDto;
import com.incidenttracker.backend.task.dto.TaskResponseDto;
import com.incidenttracker.backend.task.entity.Task;
import com.incidenttracker.backend.task.repository.TaskRepository;
import com.incidenttracker.backend.task.service.impl.TaskServiceImpl;
import com.incidenttracker.backend.user.entity.User;
import com.incidenttracker.backend.user.repository.UserRepository;

// @ExtendWith: enables Mockito support for JUnit 5 test execution.
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    // @Mock: creates repository mock so service is tested in isolation.
    @Mock
    private TaskRepository taskRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private AuditService auditService;

    @Mock
    private IncidentSlaBreachRepository breachRepository;

    @Mock
    private NotificationService notificationService;

    // @InjectMocks: creates service and injects all @Mock dependencies.
    @InjectMocks
    private TaskServiceImpl taskService;

    // @Test: marks method as executable test scenario.
    @Test
    // Test: runs the createTask_returnsResponseAndLogs scenario and checks expected outputs/side effects.
    void createTask_returnsResponseAndLogs() {
        TaskRequestDto request = TaskRequestDto.builder()
                .title("Investigate")
                .description("Check logs")
                .assignedTo(200L)
                .incidentId(300L)
                .build();

        Incident incident = new Incident();
        incident.setIncidentId(300L);
        incident.setSlaDueAt(LocalDateTime.of(2026, 2, 7, 10, 0));

        User assignedTo = new User();
        assignedTo.setUserId(200L);

        User currentUser = new User();
        currentUser.setUserId(101L);

        when(incidentRepository.findById(300L)).thenReturn(Optional.of(incident));
        when(userRepository.findById(200L)).thenReturn(Optional.of(assignedTo));
        when(securityService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setTaskId(1L);
            return task;
        });

        TaskResponseDto response = taskService.createTask(request);

        assertNotNull(response);
        assertEquals(1L, response.getTaskId());
        assertEquals("Investigate", response.getTitle());
        assertEquals(200L, response.getAssignedTo());
        assertEquals(300L, response.getIncidentId());

        verify(auditService).log(eq(incident), eq(currentUser), eq(ActionType.TASK_CREATED), anyString());
        verify(auditService).log(eq(incident), eq(currentUser), eq(ActionType.INCIDENT_STATUS_CHANGED), anyString());
        verify(notificationService).notifyEmployee(any(Task.class));
    }

    @Test
    // Test: runs the getTaskByTaskId_mapsResponse scenario and checks expected outputs/side effects.
    void getTaskByTaskId_mapsResponse() {
        Incident incident = new Incident();
        incident.setIncidentId(500L);
        User assignedTo = new User();
        assignedTo.setUserId(501L);
        User assignedBy = new User();
        assignedBy.setUserId(502L);

        Task task = Task.builder()
                .taskId(10L)
                .title("Task 10")
                .description("Desc")
                .status(TaskStatus.PENDING)
                .dueDate(LocalDateTime.of(2026, 2, 7, 11, 0))
                .createdDate(LocalDateTime.of(2026, 2, 6, 9, 0))
                .assignedTo(assignedTo)
                .assignedBy(assignedBy)
                .incident(incident)
                .build();

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        TaskResponseDto response = taskService.getTaskByTaskId(10L);

        assertEquals(10L, response.getTaskId());
        assertEquals(501L, response.getAssignedTo());
        assertEquals(502L, response.getAssignedBy());
        assertEquals(500L, response.getIncidentId());
    }

    @Test
    // Test: runs the updateTaskStatus_inProgress_byManagerAssignedBy scenario and checks expected outputs/side effects.
    void updateTaskStatus_inProgress_byManagerAssignedBy() {
        Incident incident = new Incident();
        incident.setIncidentId(700L);

        User manager = new User();
        manager.setUserId(900L);
        manager.setRole(UserRole.MANAGER);

        User assignedTo = new User();
        assignedTo.setUserId(901L);

        Task task = Task.builder()
                .taskId(7L)
                .status(TaskStatus.PENDING)
                .assignedBy(manager)
                .assignedTo(assignedTo)
                .incident(incident)
                .build();

        when(taskRepository.findById(7L)).thenReturn(Optional.of(task));
        when(securityService.getCurrentUser()).thenReturn(Optional.of(manager));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskService.updateTaskStatus(7L, "IN_PROGRESS");

        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        verify(auditService).log(eq(incident), eq(manager), eq(ActionType.TASK_STATUS_CHANGED), anyString());
    }

    @Test
    // Test: runs the updateTaskStatus_completed_resolvesIncidentAndClosesBreach scenario and checks expected outputs/side effects.
    void updateTaskStatus_completed_resolvesIncidentAndClosesBreach() {
        Incident incident = new Incident();
        incident.setIncidentId(800L);
        incident.setStatus(IncidentStatus.IN_PROGRESS);

        User employee = new User();
        employee.setUserId(1000L);
        employee.setRole(UserRole.EMPLOYEE);

        User manager = new User();
        manager.setUserId(1001L);
        manager.setRole(UserRole.MANAGER);

        Task task = Task.builder()
                .taskId(8L)
                .status(TaskStatus.IN_PROGRESS)
                .assignedTo(employee)
                .assignedBy(manager)
                .incident(incident)
                .build();

        IncidentSlaBreach breach = IncidentSlaBreach.builder()
                .incident(incident)
                .breachStatus(BreachStatus.OPEN)
                .build();

        when(taskRepository.findById(8L)).thenReturn(Optional.of(task));
        when(securityService.getCurrentUser()).thenReturn(Optional.of(employee));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.findByIncident_IncidentId(800L)).thenReturn(List.of(task));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(breachRepository.findByIncident_IncidentId(800L)).thenReturn(Optional.of(breach));

        taskService.updateTaskStatus(8L, "COMPLETED");

        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertEquals(IncidentStatus.RESOLVED, incident.getStatus());
        assertEquals(BreachStatus.RESOLVED, breach.getBreachStatus());

        verify(auditService).log(eq(incident), eq(employee), eq(ActionType.TASK_STATUS_CHANGED), anyString());
        verify(auditService).log(eq(incident), eq(employee), eq(ActionType.INCIDENT_STATUS_CHANGED), anyString());
        verify(auditService).log(eq(incident), eq(employee), eq(ActionType.INCIDENT_UPDATED), anyString());
        verify(notificationService).notifyReporterIncidentResolved(any(Incident.class));
    }

    @Test
    // Scenario: invalid status string should fail validation with BadRequestException.
    void updateTaskStatus_invalidStatus_throwsBadRequest() {
        Incident incident = new Incident();
        incident.setIncidentId(900L);

        User manager = new User();
        manager.setUserId(901L);
        manager.setRole(UserRole.MANAGER);

        User assignedTo = new User();
        assignedTo.setUserId(902L);

        Task task = Task.builder()
                .taskId(9L)
                .status(TaskStatus.PENDING)
                .assignedBy(manager)
                .assignedTo(assignedTo)
                .incident(incident)
                .build();

        when(taskRepository.findById(9L)).thenReturn(Optional.of(task));
        when(securityService.getCurrentUser()).thenReturn(Optional.of(manager));

        assertThrows(BadRequestException.class, () -> taskService.updateTaskStatus(9L, "NOT_A_STATUS"));
    }
}
