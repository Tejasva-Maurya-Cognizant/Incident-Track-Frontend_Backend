package com.incidenttracker.backend.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.incidenttracker.backend.common.enums.UserRole;
import com.incidenttracker.backend.common.enums.UserStatus;
import com.incidenttracker.backend.department.entity.Department;
import com.incidenttracker.backend.department.repository.DepartmentRepository;
import com.incidenttracker.backend.user.dto.UpdateUserDetails;
import com.incidenttracker.backend.user.entity.User;
import com.incidenttracker.backend.user.repository.UserRepository;

// Enable Mockito annotations (@Mock/@InjectMocks) for this test class.
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    // Creates a Mockito mock for isolating dependencies.
    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    // Injects mocks into the class under test.
    @InjectMocks
    private AdminService adminService;

    // Marks a method as a test case.
    @Test
    // Test: runs the toggleUserStatus_setsInactiveWhenActive scenario and checks expected outputs/side effects.
    void toggleUserStatus_setsInactiveWhenActive() {
        User user = new User();
        user.setUserId(1L);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<String> response = adminService.toggleUserStatus(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User status toggled to INACTIVE", response.getBody());
        assertEquals(UserStatus.INACTIVE, user.getStatus());
        verify(userRepository).save(user);
    }

    @Test
    // Test: runs the toggleUserStatus_setsActiveWhenInactive scenario and checks expected outputs/side effects.
    void toggleUserStatus_setsActiveWhenInactive() {
        User user = new User();
        user.setUserId(2L);
        user.setStatus(UserStatus.INACTIVE);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        ResponseEntity<String> response = adminService.toggleUserStatus(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User status toggled to ACTIVE", response.getBody());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        verify(userRepository).save(user);
    }

    @Test
    // Test: runs the toggleUserStatus_throwsNotFoundWhenMissing scenario and checks expected outputs/side effects.
    void toggleUserStatus_throwsNotFoundWhenMissing() {
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> adminService.toggleUserStatus(3L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    // Test: runs the updateUserDetails_updatesFieldsAndDepartment scenario and checks expected outputs/side effects.
    void updateUserDetails_updatesFieldsAndDepartment() {
        User user = new User();
        user.setUserId(5L);
        user.setUsername("old");
        user.setPassword("oldpass");
        user.setEmail("old@example.com");
        user.setRole(UserRole.EMPLOYEE);

        Department dept = new Department();
        dept.setDepartmentId(10L);

        UpdateUserDetails update = new UpdateUserDetails();
        update.setUsername("new");
        update.setPassword("newpass");
        update.setEmail("new@example.com");
        update.setRole(UserRole.MANAGER);
        update.setDepartmentId(10L);

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(dept));

        ResponseEntity<String> response = adminService.updateUserDetails(5L, update);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User with id 5 updated successfully", response.getBody());
        assertEquals("new", user.getUsername());
        assertTrue(new BCryptPasswordEncoder().matches("newpass", user.getPassword()));
        assertEquals("new@example.com", user.getEmail());
        assertEquals(UserRole.MANAGER, user.getRole());
        assertEquals(dept, user.getDepartment());
        verify(userRepository).save(user);
    }

    @Test
    // Test: runs the updateUserDetails_returnsNotFoundWhenMissing scenario and checks expected outputs/side effects.
    void updateUserDetails_returnsNotFoundWhenMissing() {
        when(userRepository.findById(6L)).thenReturn(Optional.empty());

        ResponseEntity<String> response = adminService.updateUserDetails(6L, new UpdateUserDetails());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found with id 6", response.getBody());
    }

    @Test
    // Test: runs the updateUserDetails_throwsBadRequestWhenDepartmentInvalid scenario and checks expected outputs/side effects.
    void updateUserDetails_throwsBadRequestWhenDepartmentInvalid() {
        User user = new User();
        user.setUserId(7L);

        UpdateUserDetails update = new UpdateUserDetails();
        update.setDepartmentId(999L);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> adminService.updateUserDetails(7L, update));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(departmentRepository).findById(999L);
    }
}
