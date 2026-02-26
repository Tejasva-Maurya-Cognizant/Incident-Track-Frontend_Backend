package com.incidenttracker.backend.user.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incidenttracker.backend.common.enums.UserRole;
import com.incidenttracker.backend.common.enums.UserStatus;
import com.incidenttracker.backend.department.entity.Department;
import com.incidenttracker.backend.department.repository.DepartmentRepository;
import com.incidenttracker.backend.user.config.JWTUtil;
import com.incidenttracker.backend.user.dto.AuthRequest;
import com.incidenttracker.backend.user.dto.UpdateUser;
import com.incidenttracker.backend.user.dto.UpdateUserDetails;
import com.incidenttracker.backend.user.dto.UserRegistrationDTO;
import com.incidenttracker.backend.user.dto.UserResponseDto;
import com.incidenttracker.backend.user.entity.User;
import com.incidenttracker.backend.user.repository.UserRepository;
import com.incidenttracker.backend.user.service.AdminService;
import com.incidenttracker.backend.user.service.UserService;
import com.incidenttracker.backend.common.security.SecurityService;

// Enable Mockito annotations (@Mock/@InjectMocks) for this test class.
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    // Creates a Mockito mock for isolating dependencies.
    @Mock
    private UserService userService;

    @Mock
    private SecurityService securityService;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private AdminService adminService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    // Injects mocks into the class under test.
    @InjectMocks
    private AuthController authController;

    // Runs before each test to prepare common setup.
    @BeforeEach
    // Setup: create shared fixtures/mocks so each test runs in a predictable state.
    void setUp() {
        objectMapper = new ObjectMapper();
        ReflectionTestUtils.setField(authController, "userRepository", userRepository);
        ReflectionTestUtils.setField(authController, "departmentRepository", departmentRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    // Marks a method as a test case.
    @Test
    // Test: runs the login_returnsTokenWhenActive scenario and checks expected
    // outputs/side effects.
    void login_returnsTokenWhenActive() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setEmail("alice@example.com");
        request.setPassword("password");

        User user = new User();
        user.setUserId(1L);
        user.setEmail("alice@example.com");
        user.setStatus(UserStatus.ACTIVE);

        when(authManager.authenticate(any(Authentication.class))).thenReturn(null);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("alice@example.com")).thenReturn("token-123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-123"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.userId").value(1L));

        verify(userRepository).findByEmail("alice@example.com");
        verify(jwtUtil).generateToken("alice@example.com");
    }

    @Test
    // Test: runs the login_returnsForbiddenWhenInactive scenario and checks
    // expected outputs/side effects.
    void login_returnsForbiddenWhenInactive() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setEmail("bob@example.com");
        request.setPassword("password");

        User user = new User();
        user.setUserId(2L);
        user.setEmail("bob@example.com");
        user.setStatus(UserStatus.INACTIVE);

        when(authManager.authenticate(any(Authentication.class))).thenReturn(null);
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.email").value("Account is deactivated"))
                .andExpect(jsonPath("$.userId").doesNotExist());

        verify(userRepository).findByEmail("bob@example.com");
    }

    @Test
    // Test: runs the register_returnsBadRequestWhenUsernameExists scenario and
    // checks expected outputs/side effects.
    void register_returnsBadRequestWhenUsernameExists() throws Exception {
        UserRegistrationDTO request = new UserRegistrationDTO();
        request.setUsername("alice");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userRepository).findByUsername("alice");
    }

    @Test
    // Test: runs the register_returnsOkWhenSuccessful scenario and checks expected
    // outputs/side effects.
    void register_returnsOkWhenSuccessful() throws Exception {
        UserRegistrationDTO request = new UserRegistrationDTO();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("password");
        request.setDepartmentId(1L);
        request.setRole(UserRole.EMPLOYEE);
        request.setStatus(UserStatus.ACTIVE);

        Department dept = new Department();
        dept.setDepartmentId(1L);

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userRepository).save(any(User.class));
    }

    @Test
    // Test: runs the getUserById_delegatesToService scenario and checks expected
    // outputs/side effects.
    void getUserById_delegatesToService() throws Exception {
        UserResponseDto dto = UserResponseDto.builder().userId(1L).username("alice").build();
        when(userService.getUserById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/auth/getUserById/{id}", 1L))
                .andExpect(status().isOk());

        verify(userService).getUserById(1L);
    }

    @Test
    // Test: runs the viewProfile_delegatesToServiceWithCurrentUser scenario and
    // checks expected outputs/side effects.
    void viewProfile_delegatesToServiceWithCurrentUser() throws Exception {
        User current = new User();
        current.setUserId(3L);
        when(securityService.getCurrentUser()).thenReturn(Optional.of(current));
        UserResponseDto dto = UserResponseDto.builder().userId(3L).username("alice").build();
        when(userService.getUserById(3L)).thenReturn(dto);

        mockMvc.perform(get("/api/auth/view-profile"))
                .andExpect(status().isOk());

        verify(userService).getUserById(3L);
    }

    @Test
    // Test: runs the getAllUsers_delegatesToService scenario and checks expected
    // outputs/side effects.
    void getAllUsers_delegatesToService() throws Exception {
        UserResponseDto dto = UserResponseDto.builder().userId(1L).username("alice").build();
        when(userService.getAllUsers()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/auth/getAllUsers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(userService).getAllUsers();
    }

    @Test
    // Test: runs the getEmployeesByDepartment_delegatesToService scenario and
    // checks expected outputs/side effects.
    void getEmployeesByDepartment_delegatesToService() throws Exception {
        User current = new User();
        current.setUserId(4L);
        when(securityService.getCurrentUser()).thenReturn(Optional.of(current));
        UserResponseDto dto = UserResponseDto.builder().userId(5L).username("emp").build();
        when(userService.getEmployeesByDepartment(4L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/auth/getEmployeesByDepartment"))
                .andExpect(status().isOk());

        verify(userService).getEmployeesByDepartment(4L);
    }

    @Test
    // Test: runs the deactivateUser_delegatesToAdminService scenario and checks
    // expected outputs/side effects.
    void deactivateUser_delegatesToAdminService() throws Exception {
        when(adminService.deactivateUser(5L)).thenReturn(ResponseEntity.ok("User deactivated successfully"));

        mockMvc.perform(patch("/api/auth/deactivateUser/{id}", 5L))
                .andExpect(status().isOk());

        verify(adminService).deactivateUser(5L);
    }

    @Test
    // Test: runs the updateUserAdmin_delegatesToService scenario and checks
    // expected outputs/side effects.
    void updateUserEmployee_delegatesToService() throws Exception {
        UpdateUser dto = new UpdateUser();
        dto.setUsername("emp");
        dto.setPassword("pass");

        User current = new User();
        current.setUserId(7L);
        when(securityService.getCurrentUser()).thenReturn(Optional.of(current));
        when(userService.updateUser(eq(7L), any(UpdateUser.class)))
                .thenReturn(ResponseEntity.ok("User with id 7 updated successfully"));

        mockMvc.perform(put("/api/auth/updateUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(userService).updateUser(eq(7L), any(UpdateUser.class));
    }

    @Test
    // Test: runs the updateUserDetails_delegatesToAdminService scenario and checks
    // expected outputs/side effects.
    void updateUserDetails_delegatesToAdminService() throws Exception {
        UpdateUserDetails dto = new UpdateUserDetails();
        dto.setUsername("admin");

        when(adminService.updateUserDetails(eq(8L), any(UpdateUserDetails.class)))
                .thenReturn(ResponseEntity.ok("User with id 8 updated successfully"));

        mockMvc.perform(put("/api/auth/updateUserDetails/{id}", 8L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(adminService).updateUserDetails(eq(8L), any(UpdateUserDetails.class));
    }
}
