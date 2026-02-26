package com.incidenttracker.backend.audit_v1.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.incidenttracker.backend.audit_v1.entity.IncidentSlaBreach;
import com.incidenttracker.backend.audit_v1.repository.IncidentSlaBreachRepository;
import com.incidenttracker.backend.common.enums.ActionType;
import com.incidenttracker.backend.common.enums.BreachStatus;
import com.incidenttracker.backend.incident.entity.Incident;
import com.incidenttracker.backend.incident.repository.IncidentRepository;
import com.incidenttracker.backend.notification.service.NotificationService;

// Enable Mockito annotations (@Mock/@InjectMocks) for this test class.
@ExtendWith(MockitoExtension.class)
class SlaComplianceServiceTest {

    // Creates a Mockito mock for isolating dependencies.
    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private IncidentSlaBreachRepository breachRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private NotificationService notificationService;

    // Injects mocks into the class under test.
    @InjectMocks
    private SlaComplianceService slaComplianceService;

    // Marks a method as a test case.
    @Test
    // Test: runs the detectSlaBreaches_createsBreachAndNotifiesAndAudits scenario and checks expected outputs/side effects.
    void detectSlaBreaches_createsBreachAndNotifiesAndAudits() {
        Incident incident = new Incident();
        incident.setIncidentId(1L);
        incident.setSlaBreached(false);
        LocalDateTime dueAt = LocalDateTime.now().minusMinutes(10);
        incident.setSlaDueAt(dueAt);

        when(incidentRepository.findSlaOverdueNotMarked(any(LocalDateTime.class)))
                .thenReturn(List.of(incident));
        when(breachRepository.existsByIncident_IncidentId(1L)).thenReturn(false);
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));

        slaComplianceService.detectSlaBreaches();

        ArgumentCaptor<IncidentSlaBreach> breachCaptor = ArgumentCaptor.forClass(IncidentSlaBreach.class);
        verify(breachRepository).save(breachCaptor.capture());

        IncidentSlaBreach breach = breachCaptor.getValue();
        assertTrue(breach.getBreachMinutes() >= 0);
        assertTrue(breach.getBreachedAt() != null);
        assertTrue(breach.getIncident() == incident);
        assertTrue(breach.getSlaDueAt().equals(dueAt));
        assertTrue(breach.getBreachStatus() == BreachStatus.OPEN);
        assertTrue("Incident not resolved within SLA time".equals(breach.getReason()));

        verify(incidentRepository).save(incident);
        verify(notificationService).notifySlaBreached(incident);
        verify(auditService).log(eq(incident), eq(null), eq(ActionType.INCIDENT_UPDATED), any(String.class));
        assertTrue(incident.getSlaBreached());
    }

    @Test
    // Test: runs the detectSlaBreaches_skipsWhenBreachAlreadyExists scenario and checks expected outputs/side effects.
    void detectSlaBreaches_skipsWhenBreachAlreadyExists() {
        Incident incident = new Incident();
        incident.setIncidentId(2L);
        incident.setSlaBreached(false);
        incident.setSlaDueAt(LocalDateTime.now().minusMinutes(5));

        when(incidentRepository.findSlaOverdueNotMarked(any(LocalDateTime.class)))
                .thenReturn(List.of(incident));
        when(breachRepository.existsByIncident_IncidentId(2L)).thenReturn(true);

        slaComplianceService.detectSlaBreaches();

        verify(breachRepository, never()).save(any(IncidentSlaBreach.class));
        verify(incidentRepository, never()).save(any(Incident.class));
        verify(notificationService, never()).notifySlaBreached(any(Incident.class));
        verify(auditService, never()).log(any(), any(), any(), any());
        assertTrue(incident.getSlaBreached());
    }
}
