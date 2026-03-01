package com.incidenttracker.backend.audit_v1.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.incidenttracker.backend.audit_v1.entity.IncidentSlaBreach;
import com.incidenttracker.backend.category.entity.Category;
import com.incidenttracker.backend.common.enums.BreachStatus;
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
class IncidentSlaBreachRepositoryTest {

    // Injects a Spring-managed bean into the test.
    @Autowired
    private IncidentSlaBreachRepository incidentSlaBreachRepository;

    @Autowired
    private TestEntityManager entityManager;

    // Marks a method as a test case.
    @Test
    // Test: runs the findByIncidentIncidentId_returnsMatch scenario and checks expected outputs/side effects.
    void findByIncidentIncidentId_returnsMatch() {
        Incident incident = persistIncident("SLA breach incident");
        IncidentSlaBreach breach = persistBreach(incident, BreachStatus.OPEN);

        Optional<IncidentSlaBreach> result = incidentSlaBreachRepository
                .findByIncident_IncidentId(incident.getIncidentId());

        assertTrue(result.isPresent());
        assertEquals(breach.getBreachId(), result.get().getBreachId());
    }

    @Test
    // Test: runs the existsByIncidentIncidentId_returnsTrueWhenPresent scenario and checks expected outputs/side effects.
    void existsByIncidentIncidentId_returnsTrueWhenPresent() {
        Incident incident = persistIncident("Exists check incident");
        persistBreach(incident, BreachStatus.OPEN);

        boolean exists = incidentSlaBreachRepository.existsByIncident_IncidentId(incident.getIncidentId());

        assertTrue(exists);
    }

    private IncidentSlaBreach persistBreach(Incident incident, BreachStatus status) {
        LocalDateTime dueAt = LocalDateTime.now().minusHours(2);
        LocalDateTime breachedAt = LocalDateTime.now().minusHours(1);

        IncidentSlaBreach breach = new IncidentSlaBreach();
        breach.setIncident(incident);
        breach.setSlaDueAt(dueAt);
        breach.setBreachedAt(breachedAt);
        breach.setBreachMinutes(60L);
        breach.setBreachStatus(status);
        breach.setReason("SLA exceeded");
        entityManager.persist(breach);
        entityManager.flush();
        return breach;
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
