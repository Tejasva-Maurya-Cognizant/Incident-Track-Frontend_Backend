package com.incidenttracker.backend.incident.service;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.*;

import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import java.util.List;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;

import com.incidenttracker.backend.audit_v1.entity.IncidentSlaBreach;

import com.incidenttracker.backend.audit_v1.repository.IncidentSlaBreachRepository;

import com.incidenttracker.backend.audit_v1.service.AuditService;

import com.incidenttracker.backend.category.entity.Category;

import com.incidenttracker.backend.category.repository.CategoryRepository;

import com.incidenttracker.backend.common.enums.ActionType;

import com.incidenttracker.backend.common.enums.BreachStatus;

import com.incidenttracker.backend.common.enums.IncidentSeverity;

import com.incidenttracker.backend.common.enums.IncidentStatus;

import com.incidenttracker.backend.common.security.SecurityService;

import com.incidenttracker.backend.incident.dto.IncidentRequestDTO;

import com.incidenttracker.backend.incident.dto.IncidentResponseDTO;

import com.incidenttracker.backend.incident.dto.IncidentStatusUpdateRequestDTO;

import com.incidenttracker.backend.incident.entity.Incident;

import com.incidenttracker.backend.incident.repository.IncidentRepository;

import com.incidenttracker.backend.incident.service.impl.IncidentServiceImpl;

import com.incidenttracker.backend.notification.service.NotificationService;

import com.incidenttracker.backend.task.repository.TaskRepository;

import com.incidenttracker.backend.user.entity.User;

// Enable Mockito annotations (@Mock/@InjectMocks) for this test class.
@ExtendWith(MockitoExtension.class)
class IncidentServiceImplTest {

    // Creates a Mockito mock for isolating dependencies.
    @Mock

    private IncidentRepository incidentRepository;

    @Mock

    private CategoryRepository categoryRepository;

    @Mock

    private SecurityService securityService;

    @Mock

    private IncidentSlaBreachRepository breachRepository;

    @Mock

    private AuditService auditService;

    @Mock

    private NotificationService notificationService;

    @Mock

    private TaskRepository taskRepository;

    // Injects mocks into the class under test.
    @InjectMocks

    private IncidentServiceImpl incidentService;

    // Test Data

    private User mockUser;

    private Category mockCategory;

    private Incident mockIncident;

    private IncidentSlaBreach mockBreach;

    // Runs before each test to prepare common setup.
    @BeforeEach

    void setUp() {

        // 1. Mock User

        mockUser = new User();

        mockUser.setUserId(1L);

        mockUser.setRole(com.incidenttracker.backend.common.enums.UserRole.EMPLOYEE);

        mockUser.setUsername("test_user");

        // 2. Mock Category (Standard SLA = 4 Hours)

        mockCategory = new Category();

        mockCategory.setCategoryId(101L);

        mockCategory.setCategoryName("Hardware");

        mockCategory.setSlaTimeHours(4);

        // 3. Mock Incident

        mockIncident = new Incident();

        mockIncident.setIncidentId(500L);

        mockIncident.setDescription("Test Incident");

        mockIncident.setReportedBy(mockUser);

        mockIncident.setCategory(mockCategory);

        mockIncident.setStatus(IncidentStatus.OPEN);

        mockIncident.setCalculatedSeverity(IncidentSeverity.HIGH);

        mockIncident.setUrgent(false);

        mockIncident.setReportedDate(LocalDateTime.now());

        mockIncident.setSlaDueAt(LocalDateTime.now().plusHours(4));

        // 4. Mock Breach (Active)

        mockBreach = new IncidentSlaBreach();

        mockBreach.setBreachId(10L);

        mockBreach.setIncident(mockIncident);

        mockBreach.setBreachStatus(BreachStatus.OPEN);

        // Default Security Context (can be overridden in specific tests)

        lenient().when(securityService.getCurrentUser()).thenReturn(Optional.of(mockUser));

    }

    // ===================================================================================

    // 1. CREATE INCIDENT TESTS

    // ===================================================================================

    // Marks a method as a test case.
    @Test

    // Provides a readable name for the test in reports.
    @DisplayName("Create Incident - Standard SLA Logic (Severity HIGH)")

    void createIncident_StandardSla_Success() {

        // Arrange

        IncidentRequestDTO request = new IncidentRequestDTO();

        request.setCategoryId(101L);

        request.setDescription("Mouse Broken");

        request.setUrgent(false);

        when(categoryRepository.findById(101L)).thenReturn(Optional.of(mockCategory));

        // Capture saved incident to verify logic

        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> {

            Incident saved = invocation.getArgument(0);

            saved.setIncidentId(500L);

            return saved;

        });

        // Act

        IncidentResponseDTO result = incidentService.createIncident(request);

        // Assert

        assertNotNull(result);

        assertEquals(500L, result.getIncidentId());

        assertEquals(4, result.getSlaHours()); // Category default

        // Logic check: 4 hours <= 6 hours -> HIGH Severity

        assertEquals(IncidentSeverity.HIGH, result.getCalculatedSeverity());

        // Verify Audit

        verify(auditService).log(any(Incident.class), eq(mockUser), eq(ActionType.INCIDENT_CREATED), anyString());

        verify(notificationService).notifyAllManager(any(Incident.class));
        verify(notificationService).notifyManagersUrgentOrCancelled(any(Incident.class));

    }

    @Test

    @DisplayName("Create Incident - Critical Override Logic (Severity CRITICAL)")

    void createIncident_CriticalOverride_Success() {

        // Arrange

        IncidentRequestDTO request = new IncidentRequestDTO();

        request.setCategoryId(101L);

        request.setDescription("Server on Fire");

        request.setUrgent(true); // <--- Urgent Override

        when(categoryRepository.findById(101L)).thenReturn(Optional.of(mockCategory));

        when(incidentRepository.save(any(Incident.class))).thenAnswer(i -> i.getArgument(0));

        // Act

        IncidentResponseDTO result = incidentService.createIncident(request);

        // Assert

        assertEquals(IncidentSeverity.CRITICAL, result.getCalculatedSeverity());

        assertEquals(4, result.getSlaHours()); // Category default still used in response

    }

    @Test

    @DisplayName("Create Incident - Fail: Category Not Found")

    void createIncident_CategoryNotFound() {

        IncidentRequestDTO request = new IncidentRequestDTO();

        request.setCategoryId(999L);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> incidentService.createIncident(request));

        verify(incidentRepository, never()).save(any());

    }

    @Test

    @DisplayName("Create Incident - Fail: User Not Authenticated")

    void createIncident_UserNotAuthenticated() {

        // Override Setup: Return empty user

        when(securityService.getCurrentUser()).thenReturn(Optional.empty());

        IncidentRequestDTO request = new IncidentRequestDTO();

        request.setCategoryId(101L);

        when(categoryRepository.findById(101L)).thenReturn(Optional.of(mockCategory));

        assertThrows(RuntimeException.class, () -> incidentService.createIncident(request));

        verify(incidentRepository, never()).save(any());

    }

    // ===================================================================================

    // 2. READ / GET TESTS

    // ===================================================================================

    @Test

    @DisplayName("Get Incidents by User - Success")

    void getIncidentsUser_Success() {

        when(incidentRepository.findByReportedBy_UserId(mockUser.getUserId()))

                .thenReturn(List.of(mockIncident));

        List<IncidentResponseDTO> result = incidentService.getIncidentsUser();

        assertFalse(result.isEmpty());

        assertEquals(1, result.size());

        assertEquals("Test Incident", result.get(0).getDescription());

    }

    @Test

    @DisplayName("Get Incident Details - Success")

    void getIncidentDetails_Success() {

        when(incidentRepository.findByIncidentIdAndReportedBy_UserId(500L, mockUser.getUserId()))

                .thenReturn(Optional.of(mockIncident));

        IncidentResponseDTO result = incidentService.getIncidentDetails(500L);

        assertNotNull(result);

        assertEquals(500L, result.getIncidentId());

    }

    @Test

    @DisplayName("Get Incidents by Severity - Success")

    void getIncidentsByUserAndCalculatedSeverity_Success() {

        when(incidentRepository.findByReportedBy_UserIdAndCalculatedSeverity(mockUser.getUserId(),
                IncidentSeverity.HIGH))

                .thenReturn(List.of(mockIncident));

        List<IncidentResponseDTO> result = incidentService
                .getIncidentsByUserAndCalculatedSeverity(IncidentSeverity.HIGH);

        assertEquals(1, result.size());

    }

    @Test

    @DisplayName("Get Incidents by Critical Flag - Success")

    void getIncidentsByUserAndUrgent_Success() {

        when(incidentRepository.findByReportedBy_UserIdAndUrgent(mockUser.getUserId(), false))

                .thenReturn(List.of(mockIncident));

        List<IncidentResponseDTO> result = incidentService.getIncidentsByUserAndUrgent(false);

        assertEquals(1, result.size());

    }

    // ===================================================================================

    // 3. WITHDRAW / CANCEL TESTS

    // ===================================================================================

    @Test

    @DisplayName("Withdraw Incident - Success: Cancels Incident and Resolves Breach")

    void withdrawIncident_Success_WithBreachResolution() {

        // Arrange

        when(incidentRepository.findByIncidentIdAndReportedBy_UserId(500L, mockUser.getUserId()))

                .thenReturn(Optional.of(mockIncident));

        // Mock finding an active breach

        when(breachRepository.findByIncident_IncidentId(500L)).thenReturn(Optional.of(mockBreach));

        when(incidentRepository.save(any(Incident.class))).thenAnswer(i -> i.getArgument(0));

        // Act

        IncidentResponseDTO result = incidentService.withdrawIncident(500L);

        // Assert

        assertEquals(IncidentStatus.CANCELLED, result.getStatus());

        // Verify Breach Resolution

        assertEquals(BreachStatus.RESOLVED, mockBreach.getBreachStatus());

        verify(breachRepository).save(mockBreach); // Ensure breach was saved

        // Verify Audit Log

        verify(auditService).log(any(), eq(mockUser), eq(ActionType.INCIDENT_WITHDRAWN), anyString());

        verify(notificationService).notifyManagersUrgentOrCancelled(any(Incident.class));

    }

    @Test

    @DisplayName("Withdraw Incident - Fail: Access Denied (Not Owner)")

    void withdrawIncident_AccessDenied() {

        // Arrange: Incident owned by User 2

        User otherUser = new User();

        otherUser.setUserId(2L);

        mockIncident.setReportedBy(otherUser);

        when(incidentRepository.findByIncidentIdAndReportedBy_UserId(500L, mockUser.getUserId()))

                .thenReturn(Optional.of(mockIncident));

        // Act & Assert

        assertThrows(AccessDeniedException.class, () -> incidentService.withdrawIncident(500L));

    }

    @Test
    @DisplayName("Withdraw Incident - Fail: Resolved Incident Cannot Be Cancelled")
    void withdrawIncident_ResolvedIncident_ThrowsIllegalState() {
        // Arrange
        mockIncident.setStatus(IncidentStatus.RESOLVED);
        when(incidentRepository.findByIncidentIdAndReportedBy_UserId(500L, mockUser.getUserId()))
                .thenReturn(Optional.of(mockIncident));

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> incidentService.withdrawIncident(500L));
        assertTrue(ex.getMessage().contains("Cannot cancel an incident that is already RESOLVED"));
        verify(incidentRepository, never()).save(any(Incident.class));
    }

    // ===================================================================================

    // 4. UPDATE INCIDENT STATUS TESTS

    // ===================================================================================

    @Test

    @DisplayName("Update Status - Success: Resolved (closes Breach)")

    void updateIncidentStatus_Resolved_ClosesBreach() {

        // Arrange

        IncidentStatusUpdateRequestDTO dto = new IncidentStatusUpdateRequestDTO();

        dto.setStatus(IncidentStatus.RESOLVED);

        dto.setNote("Fixed it");

        when(incidentRepository.findById(500L)).thenReturn(Optional.of(mockIncident));

        // Mock finding breach

        when(breachRepository.findByIncident_IncidentId(500L)).thenReturn(Optional.of(mockBreach));

        when(incidentRepository.save(any(Incident.class))).thenAnswer(i -> i.getArgument(0));

        // Act

        IncidentResponseDTO result = incidentService.updateIncidentStatus(500L, dto);

        // Assert

        assertEquals(IncidentStatus.RESOLVED, result.getStatus());

        assertEquals(BreachStatus.RESOLVED, mockBreach.getBreachStatus());

        verify(breachRepository).save(mockBreach);

        // Verify 2 audit logs: 1 for status change, 1 for breach closure

        verify(auditService, times(2)).log(any(), eq(mockUser), any(ActionType.class), anyString());

        verify(notificationService).notifyReporterIncidentResolved(any(Incident.class));

    }

    @Test

    @DisplayName("Update Status - Fail: In Progress is not allowed manually")

    void updateIncidentStatus_InProgress_ThrowsIllegalState() {

        // Arrange

        IncidentStatusUpdateRequestDTO dto = new IncidentStatusUpdateRequestDTO();

        dto.setStatus(IncidentStatus.IN_PROGRESS);

        when(incidentRepository.findById(500L)).thenReturn(Optional.of(mockIncident));

        // Act + Assert

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> incidentService.updateIncidentStatus(500L, dto));

        assertEquals("Incident status can only be changed manually to RESOLVED or CANCELLED.", ex.getMessage());

        // Breach interaction should NEVER happen for rejected transitions

        verify(breachRepository, never()).findByIncident_IncidentId(anyLong());

        verify(breachRepository, never()).save(any());

        // No audit log should be written for rejected transitions

        verify(auditService, never()).log(any(), eq(mockUser), eq(ActionType.INCIDENT_STATUS_CHANGED), anyString());

    }

    @Test

    @DisplayName("Update Status - Fail: Incident Not Found")

    void updateIncidentStatus_NotFound() {

        IncidentStatusUpdateRequestDTO dto = new IncidentStatusUpdateRequestDTO();

        dto.setStatus(IncidentStatus.RESOLVED);

        when(incidentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> incidentService.updateIncidentStatus(999L, dto));

    }

    // ===================================================================================

    // 5. ADMIN TESTS

    // ===================================================================================

    @Test

    @DisplayName("Get All Incidents (Admin) - Success")

    void getAllIncidents_Success() {

        when(incidentRepository.findAll()).thenReturn(List.of(mockIncident));

        List<IncidentResponseDTO> result = incidentService.getAllIncidents();

        assertFalse(result.isEmpty());

        assertEquals(1, result.size());

    }

}
