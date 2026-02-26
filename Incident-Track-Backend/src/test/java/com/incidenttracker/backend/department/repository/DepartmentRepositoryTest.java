package com.incidenttracker.backend.department.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.incidenttracker.backend.department.entity.Department;

// @DataJpaTest: loads only JPA/repository beans with an in-memory test database.
@DataJpaTest
// Use the "test" Spring profile (loads application-test.properties).
@ActiveProfiles("test")
class DepartmentRepositoryTest {

    // @Autowired: injects the real repository bean from Spring test context.
    @Autowired
    private DepartmentRepository departmentRepository;

    // Injects TestEntityManager to persist test data directly in JPA context.
    @Autowired
    private TestEntityManager entityManager;

    // @Test: declares a JUnit test method.
    @Test
    // Scenario: repository query returns matching department when name exists.
    void findByDepartmentName_returnsMatch() {
        Department saved = persistDepartment("IT");

        Optional<Department> result = departmentRepository.findByDepartmentName("IT");

        assertTrue(result.isPresent());
        assertEquals(saved.getDepartmentId(), result.get().getDepartmentId());
        assertEquals("IT", result.get().getDepartmentName());
    }

    @Test
    // Scenario: repository query returns empty when name does not exist.
    void findByDepartmentName_returnsEmptyWhenMissing() {
        persistDepartment("HR");

        Optional<Department> result = departmentRepository.findByDepartmentName("Finance");

        assertTrue(result.isEmpty());
    }

    // Helper: persists a department row for test setup and returns managed entity.
    private Department persistDepartment(String name) {
        Department department = Department.builder()
                .departmentName(name)
                .build();
        entityManager.persist(department);
        entityManager.flush();
        return department;
    }
}
