package com.incidenttracker.backend.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.incidenttracker.backend.common.enums.UserRole;
import com.incidenttracker.backend.common.enums.UserStatus;
import com.incidenttracker.backend.department.entity.Department;
import com.incidenttracker.backend.user.dto.UpdateUser;
import com.incidenttracker.backend.user.dto.UserResponseDto;
import com.incidenttracker.backend.user.entity.User;
import com.incidenttracker.backend.user.repository.UserRepository;

// Enable Mockito annotations (@Mock/@InjectMocks) for this test class.
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    // Creates a Mockito mock for isolating dependencies.
    @Mock
    private UserRepository userRepository;

    // Injects mocks into the class under test.
    @InjectMocks
    private UserService userService;

    // Marks a method as a test case.
    @Test
    // Test: runs the getUserById_returnsOkWhenFound scenario and checks expected
    // outputs/side effects.
    void getUserById_returnsOkWhenFound() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPassword("password");
        user.setRole(UserRole.EMPLOYEE);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDto response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        verify(userRepository).findById(1L);
    }

    @Test
    // Test: runs the getUserById_returnsNotFoundWhenMissing scenario and checks
    // expected outputs/side effects.
    void getUserById_returnsNotFoundWhenMissing() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserById(2L));
        verify(userRepository).findById(2L);
    }

    @Test
    // Test: runs the getAllUsers_returnsOkWhenNotEmpty scenario and checks expected
    // outputs/side effects.
    void getAllUsers_returnsOkWhenNotEmpty() {
        User u1 = new User();
        u1.setUserId(1L);
        User u2 = new User();
        u2.setUserId(2L);

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<UserResponseDto> response = userService.getAllUsers();

        assertEquals(2, response.size());
        verify(userRepository).findAll();
    }

    @Test
    // Test: runs the getAllUsers_returnsNotFoundWhenEmpty scenario and checks
    // expected outputs/side effects.
    void getAllUsers_returnsNotFoundWhenEmpty() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponseDto> response = userService.getAllUsers();

        assertTrue(response.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    // Test: runs the getUsersByRole_returnsOkWhenNotEmpty scenario and checks
    // expected outputs/side effects.
    void getUsersByRole_returnsOkWhenNotEmpty() {
        when(userRepository.findByRole(UserRole.MANAGER)).thenReturn(List.of(new User()));

        List<UserResponseDto> response = userService.getUsersByRole(UserRole.MANAGER);

        assertEquals(1, response.size());
        verify(userRepository).findByRole(UserRole.MANAGER);
    }

    @Test
    // Test: runs the getUsersByRole_returnsNotFoundWhenEmpty scenario and checks
    // expected outputs/side effects.
    void getUsersByRole_returnsNotFoundWhenEmpty() {
        when(userRepository.findByRole(UserRole.EMPLOYEE)).thenReturn(List.of());

        List<UserResponseDto> response = userService.getUsersByRole(UserRole.EMPLOYEE);

        assertTrue(response.isEmpty());
        verify(userRepository).findByRole(UserRole.EMPLOYEE);
    }

    @Test
    // Test: runs the getUsersByDepartment_returnsOkWhenNotEmpty scenario and checks
    // expected outputs/side effects.
    void getUsersByDepartment_returnsOkWhenNotEmpty() {
        when(userRepository.findByDepartment_DepartmentId(5L)).thenReturn(List.of(new User()));

        List<UserResponseDto> response = userService.getUsersByDepartment(5L);

        assertEquals(1, response.size());
        verify(userRepository).findByDepartment_DepartmentId(5L);
    }

    @Test
    // Test: runs the getUsersByDepartment_returnsNotFoundWhenEmpty scenario and
    // checks expected outputs/side effects.
    void getUsersByDepartment_returnsNotFoundWhenEmpty() {
        when(userRepository.findByDepartment_DepartmentId(6L)).thenReturn(List.of());

        List<UserResponseDto> response = userService.getUsersByDepartment(6L);

        assertTrue(response.isEmpty());
        verify(userRepository).findByDepartment_DepartmentId(6L);
    }

    @Test
    // Test: runs the updateUser_updatesFieldsWhenFound scenario and checks expected
    // outputs/side effects.
    void updateUser_updatesFieldsWhenFound() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("old");
        user.setPassword("oldpass");

        UpdateUser update = new UpdateUser();
        update.setUsername("new");
        update.setPassword("newpass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<String> response = userService.updateUser(1L, update);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User with id 1 updated successfully", response.getBody());
        assertEquals("new", user.getUsername());
        assertTrue(new BCryptPasswordEncoder().matches("newpass", user.getPassword()));
        verify(userRepository).save(user);
    }

    @Test
    // Test: runs the updateUser_returnsNotFoundWhenMissing scenario and checks
    // expected outputs/side effects.
    void updateUser_returnsNotFoundWhenMissing() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        ResponseEntity<String> response = userService.updateUser(10L, new UpdateUser());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found with id 10", response.getBody());
    }

    @Test
    // Test: runs the getEmployeesByDepartment_returnsEmployeesForManager scenario
    // and checks expected outputs/side effects.
    void getEmployeesByDepartment_returnsEmployeesForManager() {
        Department dept = new Department();
        dept.setDepartmentId(7L);
        User manager = new User();
        manager.setUserId(1L);
        manager.setRole(UserRole.MANAGER);
        manager.setDepartment(dept);

        User emp1 = new User();
        emp1.setUserId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(manager));
        when(userRepository.findByRoleAndDepartment_DepartmentId(UserRole.EMPLOYEE, 7L))
                .thenReturn(List.of(emp1));

        List<UserResponseDto> response = userService.getEmployeesByDepartment(1L);

        assertEquals(1, response.size());
        verify(userRepository).findByRoleAndDepartment_DepartmentId(UserRole.EMPLOYEE, 7L);
    }

    @Test
    // Test: runs the getEmployeesByDepartment_returnsNotFoundWhenNoEmployees
    // scenario and checks expected outputs/side effects.
    void getEmployeesByDepartment_returnsNotFoundWhenNoEmployees() {
        Department dept = new Department();
        dept.setDepartmentId(7L);
        User manager = new User();
        manager.setUserId(1L);
        manager.setRole(UserRole.MANAGER);
        manager.setDepartment(dept);

        when(userRepository.findById(1L)).thenReturn(Optional.of(manager));
        when(userRepository.findByRoleAndDepartment_DepartmentId(UserRole.EMPLOYEE, 7L))
                .thenReturn(List.of());

        List<UserResponseDto> response = userService.getEmployeesByDepartment(1L);

        assertTrue(response.isEmpty());
    }

    @Test
    // Test: runs the getEmployeesByDepartment_throwsWhenNotManager scenario and
    // checks expected outputs/side effects.
    void getEmployeesByDepartment_throwsWhenNotManager() {
        Department dept = new Department();
        dept.setDepartmentId(8L);
        User employee = new User();
        employee.setUserId(3L);
        employee.setRole(UserRole.EMPLOYEE);
        employee.setDepartment(dept);

        when(userRepository.findById(3L)).thenReturn(Optional.of(employee));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.getEmployeesByDepartment(3L));

        assertTrue(ex.getMessage().contains("Access denied"));
        verify(userRepository).findById(3L);
    }
}
