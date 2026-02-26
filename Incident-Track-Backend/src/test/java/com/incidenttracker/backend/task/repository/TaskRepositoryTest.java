package com.incidenttracker.backend.task.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.incidenttracker.backend.category.entity.Category;
import com.incidenttracker.backend.common.enums.IncidentSeverity;
import com.incidenttracker.backend.common.enums.TaskStatus;
import com.incidenttracker.backend.common.enums.UserRole;
import com.incidenttracker.backend.common.enums.UserStatus;
import com.incidenttracker.backend.department.entity.Department;
import com.incidenttracker.backend.incident.entity.Incident;
import com.incidenttracker.backend.task.entity.Task;
import com.incidenttracker.backend.user.entity.User;

// @DataJpaTest: starts only JPA/repository components with in-memory DB.
// This keeps repository tests isolated from web/service/security layers.
@DataJpaTest
// @ActiveProfiles: loads test-specific Spring configuration.
@ActiveProfiles("test")
class TaskRepositoryTest {

    // First use of @Autowired: injects real TaskRepository bean from test context.
/*     Injects a Spring-managed bean into the test.
    not using injectbean because we want to test the repository layer in
    isolation, without involving the service layer or other components. This
    allows us to focus on testing the data access logic of the TaskRepository
    without any interference from other layers of the application. ie, we want to
    test the repository's behavior directly, without any additional logic or
    dependencies that might be present in the service layer. By using @Autowired,
    we can directly interact with the TaskRepository and verify its functionality
    in a controlled environment. */
    @Autowired
    private TaskRepository taskRepository;

    // Injects TestEntityManager for direct persistence and flush control during setup.
    // Provides an alternative to the standard EntityManager that is specifically designed for testing JPA applications. It allows you to persist and query entities in a way that is consistent with how they would be managed in a real application, while also providing additional methods for testing purposes.
    @Autowired
    private TestEntityManager entityManager;

    // @Test: marks method as executable unit test.
    @Test
    // Test: runs the findByIncidentIncidentId_returnsTasks scenario and checks
    // expected outputs/side effects.
    void findByIncidentIncidentId_returnsTasks() {
        Incident incident = persistIncident("Network issue");
        User assignedTo = persistUser("tech1", "tech1@example.com");
        User assignedBy = persistUser("mgr1", "mgr1@example.com");

        // This line creates and persists a Task entity with the title "T1", associated with the specified incident, assignedTo user, assignedBy user, and a status of PENDING. The persistTask method handles the creation and persistence of the Task entity in the in-memory database, allowing us to set up the necessary data for our test case.
        Task task1 = persistTask("T1", incident, assignedTo, assignedBy, TaskStatus.PENDING, null); // Persists task setup data used by this query test.
        Task task2 = persistTask("T2", incident, assignedTo, assignedBy, TaskStatus.IN_PROGRESS, null);

        List<Task> result = taskRepository.findByIncident_IncidentId(incident.getIncidentId());

        List<Long> ids = result.stream().map(Task::getTaskId).toList();
        assertTrue(ids.contains(task1.getTaskId()));
        assertTrue(ids.contains(task2.getTaskId()));
    }

    @Test
    // Test: runs the findByAssignedToUserId_returnsTasks scenario and checks
    // expected outputs/side effects.
    void findByAssignedToUserId_returnsTasks() {
        Incident incident = persistIncident("Login issue");
        User assignedTo = persistUser("tech2", "tech2@example.com");
        User assignedBy = persistUser("mgr2", "mgr2@example.com");

        Task task = persistTask("AssignedTo", incident, assignedTo, assignedBy, TaskStatus.PENDING, null);

        List<Task> result = taskRepository.findByAssignedTo_UserId(assignedTo.getUserId());

        List<Long> ids = result.stream().map(Task::getTaskId).toList();
        assertTrue(ids.contains(task.getTaskId()));
    }

    @Test
    // Test: runs the findByAssignedByUserId_returnsTasks scenario and checks
    // expected outputs/side effects.
    void findByAssignedByUserId_returnsTasks() {
        Incident incident = persistIncident("Email issue");
        User assignedTo = persistUser("tech3", "tech3@example.com");
        User assignedBy = persistUser("mgr3", "mgr3@example.com");

        Task task = persistTask("AssignedBy", incident, assignedTo, assignedBy, TaskStatus.PENDING, null);

        List<Task> result = taskRepository.findByAssignedBy_UserId(assignedBy.getUserId());

        List<Long> ids = result.stream().map(Task::getTaskId).toList();
        assertTrue(ids.contains(task.getTaskId()));
    }

    @Test
    // Test: runs the findByStatus_returnsTasks scenario and checks expected
    // outputs/side effects.
    void findByStatus_returnsTasks() {
        Incident incident = persistIncident("Printer issue");
        User assignedTo = persistUser("tech4", "tech4@example.com");
        User assignedBy = persistUser("mgr4", "mgr4@example.com");

        Task task = persistTask("StatusTask", incident, assignedTo, assignedBy, TaskStatus.IN_PROGRESS, null);

        List<Task> result = taskRepository.findByStatus(TaskStatus.IN_PROGRESS);

        List<Long> ids = result.stream().map(Task::getTaskId).toList();
        assertTrue(ids.contains(task.getTaskId()));
    }

    @Test
    // Test: runs the
    // findByIncidentIncidentIdInAndCompletedDateIsNotNull_returnsCompletedTasks
    // scenario and checks expected outputs/side effects.
    void findByIncidentIncidentIdInAndCompletedDateIsNotNull_returnsCompletedTasks() {
        Incident incident1 = persistIncident("Incident 1");
        Incident incident2 = persistIncident("Incident 2");
        User assignedTo = persistUser("tech5", "tech5@example.com");
        User assignedBy = persistUser("mgr5", "mgr5@example.com");

        Task completedTask = persistTask("Completed", incident1, assignedTo, assignedBy,
                TaskStatus.COMPLETED, LocalDateTime.now().minusHours(1));
        persistTask("NotCompleted", incident2, assignedTo, assignedBy, TaskStatus.IN_PROGRESS, null);

        List<Task> result = taskRepository.findByIncident_IncidentIdInAndCompletedDateIsNotNull(
                List.of(incident1.getIncidentId(), incident2.getIncidentId()));

        List<Long> ids = result.stream().map(Task::getTaskId).toList();
        assertTrue(ids.contains(completedTask.getTaskId()));
        assertEquals(1, result.stream().filter(t -> t.getCompletedDate() != null).count());
    }

    private Task persistTask(String title, Incident incident, User assignedTo, User assignedBy,
            TaskStatus status, LocalDateTime completedDate) {
        Task task = Task.builder()
                .title(title)
                .description("desc")
                .dueDate(LocalDateTime.now().plusDays(1))
                .incident(incident)
                .assignedTo(assignedTo)
                .assignedBy(assignedBy)
                .status(TaskStatus.PENDING)
                .build();
        entityManager.persist(task); // Persist setup row so repository queries execute against real managed entities.
        entityManager.flush();
        Task saved = task;
        if (status != TaskStatus.PENDING) {
            saved.setStatus(status);
        }
        if (completedDate != null) {
            saved.setCompletedDate(completedDate);
        }
        entityManager.flush();
        return saved;
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

        User reporter = persistUser("reporter-" + description, description + "@example.com");

        Incident incident = new Incident();
        incident.setCategory(category);
        incident.setReportedBy(reporter);
        incident.setDescription(description);
        incident.setCalculatedSeverity(IncidentSeverity.LOW);
        incident.setIsCritical(false);
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

}
