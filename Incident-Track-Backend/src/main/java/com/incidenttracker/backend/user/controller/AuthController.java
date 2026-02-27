package com.incidenttracker.backend.user.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.incidenttracker.backend.common.dto.PagedResponse;
import com.incidenttracker.backend.user.config.JWTUtil;
import com.incidenttracker.backend.user.entity.User;
import com.incidenttracker.backend.user.dto.AuthRequest;
import com.incidenttracker.backend.user.dto.AuthResponse;
import com.incidenttracker.backend.user.dto.UpdateUser;
import com.incidenttracker.backend.user.dto.UpdateUserDetails;
import com.incidenttracker.backend.user.dto.UserRegistrationDTO;
import com.incidenttracker.backend.user.dto.UserResponseDto;
import com.incidenttracker.backend.user.repository.UserRepository;
import com.incidenttracker.backend.user.service.AdminService;
import com.incidenttracker.backend.user.service.UserService;

import lombok.RequiredArgsConstructor;

import com.incidenttracker.backend.common.enums.UserRole;
import com.incidenttracker.backend.common.enums.UserStatus;
import com.incidenttracker.backend.common.exception.ResourceNotFoundException;
import com.incidenttracker.backend.common.security.SecurityService;
import com.incidenttracker.backend.department.entity.Department;
import com.incidenttracker.backend.department.repository.DepartmentRepository;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/auth")
public class AuthController {
    private final UserService service;
    private final SecurityService securityService;
    private final AuthenticationManager authManager;
    private final JWTUtil jwtUtils;
    private final AdminService adminService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        // Spring Security throws BadCredentialsException before repository checks
        // because it intentionally hides whether a username exists. This prevents user
        // enumeration attacks and ensures consistent authentication error responses.
        // Authentication verifies credentials, while fetching the user retrieves domain
        // data needed by the application. They serve different purposes and should
        // remain separate.
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() == UserStatus.INACTIVE) {
            return ResponseEntity.status(403).body(new AuthResponse(null, "Account is deactivated", null, null, null));
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name(), user.getUserId());
        String email = user.getEmail();
        String username = user.getUsername();
        Long userId = user.getUserId();
        UserRole role = user.getRole();

        // return new ResponseEntity(new AuthResponse(token), HttpStatus.OK);

        return ResponseEntity.ok(new AuthResponse(token, username, email, userId, role));
    }

    @Autowired
    UserRepository userRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRegistrationDTO request) {
        // Check if user already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());
        user.setDepartment(department);

        // 2. Handle the Department lookup (Fixes your error)
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(
                            () -> new RuntimeException("Department not found with ID: " + request.getDepartmentId()));
            user.setDepartment(dept);
        }
        // Encode the password using BCrypt
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        // Create and save the new user
        // User user = new User();
        // user.setUsername(request.getUsername());
        user.setPassword(encodedPassword);
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/getUserById/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getUserById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','MANAGER')")
    @GetMapping("/view-profile")
    public ResponseEntity<UserResponseDto> getUserProfile() {
        User currentUser = securityService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Authentication required"));
        return ResponseEntity.ok(service.getUserById(currentUser.getUserId()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/getAllUsers")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }

    /**
     * GET /api/auth/getAllUsers/paged?page=0&size=10&sortBy=username&sortDir=asc
     */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/getAllUsers/paged")
    public ResponseEntity<PagedResponse<UserResponseDto>> getAllUsersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(service.getAllUsersPaged(pageable));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/getEmployeesByDepartment")
    public ResponseEntity<List<UserResponseDto>> getEmployeesByDepartment() {
        User currentUser = securityService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Authentication required"));
        return ResponseEntity.ok(service.getEmployeesByDepartment(currentUser.getUserId()));
    }

    /**
     * GET /api/auth/getEmployeesByDepartment/paged
     */
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/getEmployeesByDepartment/paged")
    public ResponseEntity<PagedResponse<UserResponseDto>> getEmployeesByDepartmentPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        User currentUser = securityService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Authentication required"));
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(service.getEmployeesByDepartmentPaged(currentUser.getUserId(), pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/toggleUserStatus/{id}")
    public ResponseEntity<String> toggleUserStatus(@PathVariable Long id) {
        return adminService.toggleUserStatus(id);
    }

    // @PreAuthorize("hasRole('ADMIN')")
    // @DeleteMapping("/deleteRecord/{id}")
    // public ResponseEntity<String>deleteRecord(@PathVariable int id){
    // return service.deleteRecord(id);
    // }

    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    @PutMapping("/updateUser")
    public ResponseEntity<String> updateUserEmployee(@RequestBody UpdateUser dto) {
        User currentUser = securityService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Authentication required"));
        Long id = currentUser.getUserId();
        return service.updateUser(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updateUserDetails/{id}")
    public ResponseEntity<String> updateUserDetails(@PathVariable Long id,
            @RequestBody UpdateUserDetails updateUserDetail) {
        return adminService.updateUserDetails(id, updateUserDetail);
    }

}