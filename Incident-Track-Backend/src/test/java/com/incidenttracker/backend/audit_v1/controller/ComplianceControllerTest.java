package com.incidenttracker.backend.audit_v1.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.incidenttracker.backend.audit_v1.entity.AuditLog;
import com.incidenttracker.backend.audit_v1.entity.IncidentSlaBreach;
import com.incidenttracker.backend.audit_v1.repository.AuditLogRepository;
import com.incidenttracker.backend.audit_v1.repository.IncidentSlaBreachRepository;
import com.incidenttracker.backend.common.enums.ActionType;
import com.incidenttracker.backend.common.enums.BreachStatus;
import com.incidenttracker.backend.common.enums.IncidentStatus;
import com.incidenttracker.backend.incident.entity.Incident;
import com.incidenttracker.backend.user.entity.User;

// Enable Mockito annotations (@Mock/@InjectMocks) for this test class.
@ExtendWith(MockitoExtension.class)
class ComplianceControllerTest {

    private MockMvc mockMvc;

    // Creates a Mockito mock for isolating dependencies.
    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private IncidentSlaBreachRepository breachRepository;

    // Injects mocks into the class under test.
    @InjectMocks
    private ComplianceController complianceController;

    // Runs before each test to prepare common setup.
    @BeforeEach
    // Setup: create shared fixtures/mocks so each test runs in a predictable state.
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(complianceController).build();
    }

    // Marks a method as a test case.
    @Test
    // Test: runs the getAllAuditLogs_returnsOkAndMappedList scenario and checks expected outputs/side effects.
    void getAllAuditLogs_returnsOkAndMappedList() throws Exception {
        Incident incident = new Incident();
        incident.setIncidentId(10L);
        User user = new User();
        user.setUserId(20L);
        user.setUsername("alice");

        AuditLog log = new AuditLog();
        log.setLogId(1L);
        log.setIncident(incident);
        log.setUser(user);
        log.setActionType(ActionType.INCIDENT_CREATED);
        log.setTimestamp(LocalDateTime.now());
        log.setDetails("created");

        when(auditLogRepository.findAll()).thenReturn(List.of(log));

        mockMvc.perform(get("/api/admin/compliance/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].logId").value(1L))
                .andExpect(jsonPath("$[0].incidentId").value(10L))
                .andExpect(jsonPath("$[0].userId").value(20L))
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[0].actionType").value("INCIDENT_CREATED"))
                .andExpect(jsonPath("$[0].details").value("created"));

        verify(auditLogRepository).findAll();
    }

    @Test
    // Test: runs the getAuditLogsByIncident_returnsOkAndMappedList scenario and checks expected outputs/side effects.
    void getAuditLogsByIncident_returnsOkAndMappedList() throws Exception {
        Long incidentId = 99L;
        Incident incident = new Incident();
        incident.setIncidentId(incidentId);
        User user = new User();
        user.setUserId(50L);
        user.setUsername("bob");

        AuditLog log = new AuditLog();
        log.setLogId(2L);
        log.setIncident(incident);
        log.setUser(user);
        log.setActionType(ActionType.TASK_CREATED);
        log.setTimestamp(LocalDateTime.now());
        log.setDetails("task created");

        when(auditLogRepository.findByIncident_IncidentIdOrderByTimestampDesc(incidentId))
                .thenReturn(List.of(log));

        mockMvc.perform(get("/api/admin/compliance/audit-logs/{incidentId}", incidentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].logId").value(2L))
                .andExpect(jsonPath("$[0].incidentId").value(99L))
                .andExpect(jsonPath("$[0].userId").value(50L))
                .andExpect(jsonPath("$[0].username").value("bob"))
                .andExpect(jsonPath("$[0].actionType").value("TASK_CREATED"))
                .andExpect(jsonPath("$[0].details").value("task created"));

        verify(auditLogRepository).findByIncident_IncidentIdOrderByTimestampDesc(incidentId);
    }

    @Test
    // Test: runs the getAllBreaches_returnsOkAndMappedList scenario and checks expected outputs/side effects.
    void getAllBreaches_returnsOkAndMappedList() throws Exception {
        Incident incident = new Incident();
        incident.setIncidentId(7L);
        incident.setStatus(IncidentStatus.OPEN);

        IncidentSlaBreach breach = new IncidentSlaBreach();
        breach.setBreachId(3L);
        breach.setIncident(incident);
        breach.setSlaDueAt(LocalDateTime.now().minusHours(2));
        breach.setBreachedAt(LocalDateTime.now().minusHours(1));
        breach.setBreachMinutes(60L);
        breach.setBreachStatus(BreachStatus.OPEN);
        breach.setReason("SLA exceeded");

        when(breachRepository.findAll()).thenReturn(List.of(breach));

        mockMvc.perform(get("/api/admin/compliance/breaches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].breachId").value(3L))
                .andExpect(jsonPath("$[0].incidentId").value(7L))
                .andExpect(jsonPath("$[0].incidentStatus").value("OPEN"))
                .andExpect(jsonPath("$[0].breachMinutes").value(60L))
                .andExpect(jsonPath("$[0].breachStatus").value("OPEN"))
                .andExpect(jsonPath("$[0].reason").value("SLA exceeded"));

        verify(breachRepository).findAll();
    }

    @Test
    // Test: runs the getAuditLogsByActionType_returnsOkAndMappedList scenario and checks expected outputs/side effects.
    void getAuditLogsByActionType_returnsOkAndMappedList() throws Exception {
        Incident incident = new Incident();
        incident.setIncidentId(5L);
        User user = new User();
        user.setUserId(6L);
        user.setUsername("charlie");

        AuditLog log = new AuditLog();
        log.setLogId(4L);
        log.setIncident(incident);
        log.setUser(user);
        log.setActionType(ActionType.NOTE_ADDED);
        log.setTimestamp(LocalDateTime.now());
        log.setDetails("note added");

        when(auditLogRepository.findByActionTypeOrderByTimestampDesc(ActionType.NOTE_ADDED))
                .thenReturn(List.of(log));

        mockMvc.perform(get("/api/admin/compliance/audit-log/{actionType}", "note_added"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].logId").value(4L))
                .andExpect(jsonPath("$[0].incidentId").value(5L))
                .andExpect(jsonPath("$[0].userId").value(6L))
                .andExpect(jsonPath("$[0].username").value("charlie"))
                .andExpect(jsonPath("$[0].actionType").value("NOTE_ADDED"))
                .andExpect(jsonPath("$[0].details").value("note added"));

        verify(auditLogRepository).findByActionTypeOrderByTimestampDesc(ActionType.NOTE_ADDED);
    }
}
