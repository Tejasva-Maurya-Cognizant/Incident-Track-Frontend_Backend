package com.incidenttracker.backend.audit_v1.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.incidenttracker.backend.audit_v1.entity.AuditLog;
import com.incidenttracker.backend.audit_v1.repository.AuditLogRepository;
import com.incidenttracker.backend.common.enums.ActionType;
import com.incidenttracker.backend.incident.entity.Incident;
import com.incidenttracker.backend.user.entity.User;

// Enable Mockito annotations (@Mock/@InjectMocks) for this test class.
@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    // Creates a Mockito mock for isolating dependencies.
    @Mock
    private AuditLogRepository auditLogRepository;

    // Injects mocks into the class under test.
    @InjectMocks
    private AuditService auditService;

    // Marks a method as a test case.
    @Test
    // Test: runs the log_savesAuditLogWithExpectedFields scenario and checks expected outputs/side effects.
    void log_savesAuditLogWithExpectedFields() {
        Incident incident = new Incident();
        User user = new User();
        ActionType actionType = ActionType.INCIDENT_CREATED;
        String details = "Created incident";

        auditService.log(incident, user, actionType, details);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals(incident, saved.getIncident());
        assertEquals(user, saved.getUser());
        assertEquals(actionType, saved.getActionType());
        assertEquals(details, saved.getDetails());
        assertNull(saved.getTimestamp());
    }
}
