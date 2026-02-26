package com.incidenttracker.backend.user.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.incidenttracker.backend.common.enums.UserRole;
import com.incidenttracker.backend.common.enums.UserStatus;
import com.incidenttracker.backend.department.entity.Department;
import com.incidenttracker.backend.user.entity.User;

// Slice test: loads only JPA components with an in-memory DB setup.
@DataJpaTest
// Use the "test" Spring profile (loads application-test.properties).
@ActiveProfiles("test")
class UserRepositoryTest {

    // Injects a Spring-managed bean into the test.
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    // Marks a method as a test case.
    @Test
    // Test: runs the findByEmail_returnsMatch scenario and checks expected outputs/side effects.
    void findByEmail_returnsMatch() {
        Department department = persistDepartment("IT");
        User saved = persistUser("alice", uniqueEmail(), UserRole.EMPLOYEE, department);

        Optional<User> result = userRepository.findByEmail(saved.getEmail());

        assertTrue(result.isPresent());
        assertEquals(saved.getUserId(), result.get().getUserId());
        assertEquals(saved.getEmail(), result.get().getEmail());
    }

    @Test
    // Test: runs the findByUsername_returnsMatch scenario and checks expected outputs/side effects.
    void findByUsername_returnsMatch() {
        Department department = persistDepartment("HR");
        User saved = persistUser("bob-" + unique(), uniqueEmail(), UserRole.MANAGER, department);

        Optional<User> result = userRepository.findByUsername(saved.getUsername());

        assertTrue(result.isPresent());
        assertEquals(saved.getUserId(), result.get().getUserId());
        assertEquals(saved.getUsername(), result.get().getUsername());
    }

    @Test
    // Test: runs the findByRole_returnsMatches scenario and checks expected outputs/side effects.
    void findByRole_returnsMatches() {
        Department department = persistDepartment("Support");
        User u1 = persistUser("user1-" + unique(), uniqueEmail(), UserRole.EMPLOYEE, department);
        User u2 = persistUser("user2-" + unique(), uniqueEmail(), UserRole.EMPLOYEE, department);
        persistUser("manager-" + unique(), uniqueEmail(), UserRole.MANAGER, department);

        List<User> result = userRepository.findByRole(UserRole.EMPLOYEE);

        List<Long> ids = result.stream().map(User::getUserId).toList();
        assertTrue(ids.contains(u1.getUserId()));
        assertTrue(ids.contains(u2.getUserId()));
    }

    @Test
    // Test: runs the findByDepartmentDepartmentId_returnsMatches scenario and checks expected outputs/side effects.
    void findByDepartmentDepartmentId_returnsMatches() {
        Department it = persistDepartment("IT");
        Department hr = persistDepartment("HR");
        User itUser = persistUser("it-" + unique(), uniqueEmail(), UserRole.EMPLOYEE, it);
        persistUser("hr-" + unique(), uniqueEmail(), UserRole.EMPLOYEE, hr);

        List<User> result = userRepository.findByDepartment_DepartmentId(it.getDepartmentId());

        List<Long> ids = result.stream().map(User::getUserId).toList();
        assertTrue(ids.contains(itUser.getUserId()));
    }

    @Test
    // Test: runs the findByRoleAndDepartmentDepartmentId_returnsMatches scenario and checks expected outputs/side effects.
    void findByRoleAndDepartmentDepartmentId_returnsMatches() {
        Department it = persistDepartment("IT");
        Department hr = persistDepartment("HR");
        User itManager = persistUser("mgr-" + unique(), uniqueEmail(), UserRole.MANAGER, it);
        persistUser("emp-" + unique(), uniqueEmail(), UserRole.EMPLOYEE, it);
        persistUser("hr-mgr-" + unique(), uniqueEmail(), UserRole.MANAGER, hr);

        List<User> result = userRepository.findByRoleAndDepartment_DepartmentId(UserRole.MANAGER, it.getDepartmentId());

        List<Long> ids = result.stream().map(User::getUserId).toList();
        assertTrue(ids.contains(itManager.getUserId()));
        assertEquals(1, ids.size());
    }

    private Department persistDepartment(String name) {
        Department department = Department.builder()
                .departmentName(name)
                .build();
        entityManager.persist(department);
        entityManager.flush();
        return department;
    }

    private User persistUser(String username, String email, UserRole role, Department department) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password");
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setDepartment(department);
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    private String unique() {
        return String.valueOf(System.nanoTime());
    }

    private String uniqueEmail() {
        return "user-" + unique() + "@example.com";
    }

}
