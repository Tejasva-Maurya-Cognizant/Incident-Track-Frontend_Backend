package com.incidenttracker.backend.audit_v1.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.incidenttracker.backend.audit_v1.entity.AuditLog;
import com.incidenttracker.backend.category.entity.Category;
import com.incidenttracker.backend.common.enums.ActionType;
import com.incidenttracker.backend.common.enums.IncidentSeverity;
import com.incidenttracker.backend.common.enums.UserRole;
import com.incidenttracker.backend.common.enums.UserStatus;
import com.incidenttracker.backend.department.entity.Department;
import com.incidenttracker.backend.incident.entity.Incident;
import com.incidenttracker.backend.user.entity.User;

// Slice test: loads only JPA components with an in-memory DB setup.
@DataJpaTest
// Use the "test" Spring profile (loads application-test.properties).
@ActiveProfiles("test")
class AuditLogRepositoryTest {

    // Injects a Spring-managed bean into the test.
    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private TestEntityManager entityManager;

    // Marks a method as a test case.
    @Test
    // Test: runs the findByIncidentIncidentIdOrderByTimestampDesc_returnsOrderedLogs scenario and checks expected outputs/side effects.
    void findByIncidentIncidentIdOrderByTimestampDesc_returnsOrderedLogs() {
        Incident incident = persistIncident("Audit incident");
        String userToken = unique();
        User user = persistUser("audit-user-" + userToken, "audit-user-" + userToken + "@example.com");
        LocalDateTime baseTime = LocalDateTime.of(2026, 3, 1, 10, 0, 0);

        AuditLog older = persistAuditLog(incident, user, ActionType.INCIDENT_CREATED, "created", baseTime);
        AuditLog newer = persistAuditLog(incident, user, ActionType.INCIDENT_UPDATED, "updated", baseTime.plusSeconds(1));

        List<AuditLog> result = auditLogRepository
                .findByIncident_IncidentIdOrderByTimestampDesc(incident.getIncidentId());

        assertEquals(2, result.size());
        assertEquals(newer.getLogId(), result.get(0).getLogId());
        assertEquals(older.getLogId(), result.get(1).getLogId());
    }

    @Test
    // Test: runs the findByActionTypeOrderByTimestampDesc_returnsOnlyMatchingActionType scenario and checks expected outputs/side effects.
    void findByActionTypeOrderByTimestampDesc_returnsOnlyMatchingActionType() {
        Incident incident = persistIncident("Action incident");
        String userToken = unique();
        User user = persistUser("action-user-" + userToken, "action-user-" + userToken + "@example.com");
        LocalDateTime baseTime = LocalDateTime.of(2026, 3, 1, 11, 0, 0);

        AuditLog matchOlder = persistAuditLog(incident, user, ActionType.TASK_CREATED, "task created", baseTime);
        AuditLog matchNewer = persistAuditLog(incident, user, ActionType.TASK_CREATED, "task created again",
                baseTime.plusSeconds(1));
        persistAuditLog(incident, user, ActionType.NOTE_ADDED, "note added", baseTime.plusSeconds(2));

        List<AuditLog> result = auditLogRepository.findByActionTypeOrderByTimestampDesc(ActionType.TASK_CREATED);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(a -> a.getActionType() == ActionType.TASK_CREATED));
        assertEquals(matchNewer.getLogId(), result.get(0).getLogId());
        assertEquals(matchOlder.getLogId(), result.get(1).getLogId());
    }

    private AuditLog persistAuditLog(Incident incident, User user, ActionType actionType, String details,
            LocalDateTime timestamp) {
        AuditLog log = new AuditLog();
        log.setIncident(incident);
        log.setUser(user);
        log.setActionType(actionType);
        log.setDetails(details);
        log.setTimestamp(timestamp);
        entityManager.persist(log);
        entityManager.flush();
        return log;
    }

    private Incident persistIncident(String description) {
        Department department = Department.builder().departmentName("IT").build();
        entityManager.persist(department);

        Category category = Category.builder()
                .categoryName("Hardware")
                .slaTimeHours(4)
                .isVisible(true)
                .department(department)
                .build();
        entityManager.persist(category);

        String reporterToken = unique();
        User reporter = persistUser("reporter-" + reporterToken, "reporter-" + reporterToken + "@example.com");

        Incident incident = new Incident();
        incident.setCategory(category);
        incident.setReportedBy(reporter);
        incident.setDescription(description);
        incident.setCalculatedSeverity(IncidentSeverity.LOW);
        incident.setUrgent(false);
        entityManager.persist(incident);
        entityManager.flush();
        return incident;
    }

    private User persistUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password");
        user.setRole(UserRole.EMPLOYEE);
        user.setStatus(UserStatus.ACTIVE);
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    private String unique() {
        return String.valueOf(System.nanoTime());
    }
}
