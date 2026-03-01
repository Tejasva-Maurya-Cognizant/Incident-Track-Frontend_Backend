package com.incidenttracker.backend.incident.repository;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

// --- Imports (Verified) --- 
import com.incidenttracker.backend.category.entity.Category;
import com.incidenttracker.backend.common.enums.IncidentSeverity;
import com.incidenttracker.backend.common.enums.IncidentStatus;
import com.incidenttracker.backend.common.enums.UserRole;
import com.incidenttracker.backend.common.enums.UserStatus;
import com.incidenttracker.backend.department.entity.Department;
import com.incidenttracker.backend.incident.entity.Incident;
import com.incidenttracker.backend.user.entity.User;

// Slice test: loads only JPA components with an in-memory DB setup.
@DataJpaTest
// Use the "test" Spring profile (loads application-test.properties).
@ActiveProfiles("test")
class IncidentRepositoryTest {

    // Injects a Spring-managed bean into the test.
    @Autowired

    private IncidentRepository incidentRepository;

    @Autowired

    private TestEntityManager entityManager;

    // Shared dummy data

    private User user1;

    private User user2;

    private Department departmentIT;

    private Department departmentHR;

    private Category categorySoftware;

    private Category categoryPayroll;

    // Runs before each test to prepare common setup.
    @BeforeEach

    void setUp() {

        // --- 1. Create Departments ---

        departmentIT = new Department();

        departmentIT.setDepartmentName("IT Dept");

        entityManager.persist(departmentIT);

        departmentHR = new Department();

        departmentHR.setDepartmentName("HR Dept");

        entityManager.persist(departmentHR);

        // --- 2. Create Users ---

        user1 = new User();

        user1.setEmail("alice@example.com");

        user1.setPassword("pass123");

        user1.setUsername("AliceUser");

        user1.setRole(UserRole.EMPLOYEE);

        user1.setStatus(UserStatus.ACTIVE);

        entityManager.persist(user1);

        user2 = new User();

        user2.setEmail("bob@example.com");

        user2.setPassword("pass123");

        user2.setUsername("BobUser");

        user2.setRole(UserRole.EMPLOYEE);

        user2.setStatus(UserStatus.ACTIVE);

        entityManager.persist(user2);

        // --- 3. Create Categories ---

        categorySoftware = new Category();

        categorySoftware.setCategoryName("Software Issue");

        categorySoftware.setSlaTimeHours(24);

        categorySoftware.setDepartment(departmentIT);

        categorySoftware.setIsVisible(true);

        entityManager.persist(categorySoftware);

        categoryPayroll = new Category();

        categoryPayroll.setCategoryName("Payroll Issue");

        categoryPayroll.setSlaTimeHours(48);

        categoryPayroll.setDepartment(departmentHR);

        categoryPayroll.setIsVisible(true);

        entityManager.persist(categoryPayroll);

        entityManager.flush();

    }

    // --- SMART HELPER METHOD (Fixes the "Status reset to OPEN" bug) ---

    private Incident createIncident(User user, Category category, String desc, IncidentStatus status,
            IncidentSeverity severity) {

        Incident i = new Incident();

        i.setReportedBy(user);

        i.setCategory(category);

        i.setDescription(desc);

        // Note: setting status here doesn't matter yet because @PrePersist overrides
        // it!

        i.setCalculatedSeverity(severity);

        i.setUrgent(false);

        // 1. Initial Save (Triggers @PrePersist -> sets Status to OPEN)

        Incident savedIncident = incidentRepository.save(i);

        // 2. Fix Status if needed

        // If we wanted RESOLVED, but @PrePersist forced it to OPEN, we must save again.

        if (status != IncidentStatus.OPEN) {

            savedIncident.setStatus(status);

            savedIncident = incidentRepository.save(savedIncident); // Update

        }

        return savedIncident;

    }

    // ================= FULL TEST SUITE (8 CASES) =================

    // Marks a method as a test case.
    @Test

    // Provides a readable name for the test in reports.
    @DisplayName("1. Find by User ID")

    void testFindByReportedBy_UserId() {

        createIncident(user1, categorySoftware, "Bug 1", IncidentStatus.OPEN, IncidentSeverity.LOW);

        createIncident(user1, categorySoftware, "Bug 2", IncidentStatus.OPEN, IncidentSeverity.LOW);

        createIncident(user2, categorySoftware, "Bug 3", IncidentStatus.OPEN, IncidentSeverity.LOW);

        List<Incident> result = incidentRepository.findByReportedBy_UserId(user1.getUserId());

        assertThat(result).hasSize(2); // Should only get Alice's 2 incidents

    }

    @Test

    @DisplayName("2. Find by User ID and Status")

    void testFindByReportedBy_UserIdAndStatus() {

        // This will now correctly save as OPEN

        createIncident(user1, categorySoftware, "Open Issue", IncidentStatus.OPEN, IncidentSeverity.LOW);

        // This will now correctly save as RESOLVED (because of the smart helper)

        createIncident(user1, categorySoftware, "Resolved Issue", IncidentStatus.RESOLVED, IncidentSeverity.LOW);

        List<Incident> result = incidentRepository.findByReportedBy_UserIdAndStatus(

                user1.getUserId(), IncidentStatus.OPEN);

        assertThat(result).hasSize(1);

        assertThat(result.get(0).getDescription()).isEqualTo("Open Issue");

    }

    @Test

    @DisplayName("3. Find by User ID and Severity")

    void testFindByReportedBy_UserIdAndCalculatedSeverity() {

        createIncident(user1, categorySoftware, "High Sev", IncidentStatus.OPEN, IncidentSeverity.HIGH);

        createIncident(user1, categorySoftware, "Low Sev", IncidentStatus.OPEN, IncidentSeverity.LOW);

        List<Incident> result = incidentRepository.findByReportedBy_UserIdAndCalculatedSeverity(

                user1.getUserId(), IncidentSeverity.HIGH);

        assertThat(result).hasSize(1);

        assertThat(result.get(0).getDescription()).isEqualTo("High Sev");

    }

    @Test

    @DisplayName("4. Find by User ID and Critical Flag")

    void testFindByReportedBy_UserIdAndUrgent() {

        Incident crit = createIncident(user1, categorySoftware, "Critical", IncidentStatus.OPEN, IncidentSeverity.HIGH);

        crit.setUrgent(true);

        incidentRepository.save(crit);

        Incident nonCrit = createIncident(user1, categorySoftware, "Normal", IncidentStatus.OPEN,
                IncidentSeverity.HIGH);

        nonCrit.setUrgent(false);

        incidentRepository.save(nonCrit);

        List<Incident> result = incidentRepository.findByReportedBy_UserIdAndUrgent(

                user1.getUserId(), true);

        assertThat(result).hasSize(1);

        assertThat(result.get(0).getDescription()).isEqualTo("Critical");

    }

    @Test

    @DisplayName("5. Find Specific Incident by Incident ID and User ID")

    void testFindByIncidentIdAndReportedBy_UserId() {

        Incident inc1 = createIncident(user1, categorySoftware, "My Incident", IncidentStatus.OPEN,
                IncidentSeverity.LOW);

        // Correct User -> Should find

        Optional<Incident> found = incidentRepository.findByIncidentIdAndReportedBy_UserId(

                inc1.getIncidentId(), user1.getUserId());

        assertThat(found).isPresent();

        // Wrong User -> Should NOT find

        Optional<Incident> notFound = incidentRepository.findByIncidentIdAndReportedBy_UserId(

                inc1.getIncidentId(), user2.getUserId());

        assertThat(notFound).isEmpty();

    }

    @Test

    @DisplayName("6. Find SLA Overdue (Custom Query)")

    void testFindSlaOverdueNotMarked() {

        // --- Case A: Genuine Overdue Incident (Should be FOUND) ---

        // 1. Create normally

        Incident overdue = createIncident(user1, categorySoftware, "Overdue Incident", IncidentStatus.IN_PROGRESS,
                IncidentSeverity.HIGH);

        // 2. Manually set time to PAST and update

        overdue.setSlaDueAt(LocalDateTime.now().minusHours(1));

        overdue.setSlaBreached(false);

        incidentRepository.save(overdue);

        // --- Case B: Resolved Incident (Should NOT be found) ---

        // 1. Create as RESOLVED (The helper ensures it is actually RESOLVED)

        Incident resolved = createIncident(user1, categorySoftware, "Resolved Incident", IncidentStatus.RESOLVED,
                IncidentSeverity.HIGH);

        // 2. Set time to PAST

        resolved.setSlaDueAt(LocalDateTime.now().minusHours(1));

        resolved.setSlaBreached(false);

        incidentRepository.save(resolved);

        // --- Case C: Future SLA Incident (Should NOT be found) ---

        // 1. Create normally

        Incident future = createIncident(user1, categorySoftware, "Future Incident", IncidentStatus.OPEN,
                IncidentSeverity.HIGH);

        // 2. Set time to FUTURE

        future.setSlaDueAt(LocalDateTime.now().plusHours(5));

        incidentRepository.save(future);

        // --- Act ---

        List<Incident> breachedList = incidentRepository.findSlaOverdueNotMarked(LocalDateTime.now());

        // --- Assert ---

        assertThat(breachedList).hasSize(1);

        assertThat(breachedList.get(0).getDescription()).isEqualTo("Overdue Incident");

    }

    @Test

    @DisplayName("7. Find by Reported Date Between")

    void testFindByReportedDateBetween() {

        createIncident(user1, categorySoftware, "Recent", IncidentStatus.OPEN, IncidentSeverity.LOW);

        LocalDateTime now = LocalDateTime.now();

        // Search range: Yesterday to Tomorrow

        List<Incident> result = incidentRepository.findByReportedDateBetween(

                now.minusDays(1), now.plusDays(1));

        assertThat(result).isNotEmpty();

    }

    @Test

    @DisplayName("8. Find by Date Range AND Department")

    void testFindByReportedDateBetweenAndCategory_Department_DepartmentId() {

        // Incident in IT Dept

        createIncident(user1, categorySoftware, "IT Incident", IncidentStatus.OPEN, IncidentSeverity.LOW);

        // Incident in HR Dept

        createIncident(user1, categoryPayroll, "HR Incident", IncidentStatus.OPEN, IncidentSeverity.LOW);

        LocalDateTime now = LocalDateTime.now();

        // Act: Search only for IT Department in date range

        List<Incident> itIncidents = incidentRepository.findByReportedDateBetweenAndCategory_Department_DepartmentId(

                now.minusDays(1),

                now.plusDays(1),

                departmentIT.getDepartmentId()

        );

        // Assert

        assertThat(itIncidents).hasSize(1);

        assertThat(itIncidents.get(0).getDescription()).isEqualTo("IT Incident");

        // Verify we didn't get the HR incident

        assertThat(itIncidents.get(0).getCategory().getDepartment().getDepartmentName()).isEqualTo("IT Dept");

    }

}
