package com.incidenttracker.backend.user.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import com.incidenttracker.backend.common.dto.PagedResponse;
import com.incidenttracker.backend.common.enums.UserRole;
import com.incidenttracker.backend.common.exception.ResourceNotFoundException;
import com.incidenttracker.backend.user.dto.UpdateUser;
import com.incidenttracker.backend.user.dto.UserResponseDto;
import com.incidenttracker.backend.user.entity.User;
import com.incidenttracker.backend.user.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    UserRepository repo;

    public UserResponseDto getUserById(@PathVariable Long id) {
        User user = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
        return mapToResponse(user);
    }

    public List<UserResponseDto> getAllUsers() {
        return repo.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<UserResponseDto> getUsersByRole(UserRole role) {
        return repo.findByRole(role).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<UserResponseDto> getUsersByDepartment(Long departmentId) {
        return repo.findByDepartment_DepartmentId(departmentId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ResponseEntity<String> updateUser(Long id, UpdateUser updateUserObj) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return repo.findById(id)
                .map(user -> {
                    if (updateUserObj.getUsername() != null)
                        user.setUsername(updateUserObj.getUsername());
                    if (updateUserObj.getPassword() != null)
                        user.setPassword(passwordEncoder.encode(updateUserObj.getPassword()));
                    repo.save(user);
                    return ResponseEntity.ok("User with id " + id + " updated successfully");
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found with id " + id));
    }

    public List<UserResponseDto> getEmployeesByDepartment(Long userId) {
        User user = repo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        UserRole userRole = user.getRole();
        Long departmentId = user.getDepartment().getDepartmentId();

        if (userRole == UserRole.MANAGER) {
            List<User> users = repo.findByRoleAndDepartment_DepartmentId(UserRole.EMPLOYEE, departmentId);
            return users.stream().map(this::mapToResponse).toList();
        } else {
            throw new RuntimeException("Access denied. Only managers can view employees in their department.");
        }
    }

    // ---- Paginated versions ----

    public PagedResponse<UserResponseDto> getAllUsersPaged(Pageable pageable) {
        Page<User> page = repo.findAll(pageable);
        return toPagedResponse(page);
    }

    public PagedResponse<UserResponseDto> getUsersByRolePaged(UserRole role, Pageable pageable) {
        Page<User> page = repo.findByRole(role, pageable);
        return toPagedResponse(page);
    }

    public PagedResponse<UserResponseDto> getUsersByDepartmentPaged(Long departmentId, Pageable pageable) {
        Page<User> page = repo.findByDepartment_DepartmentId(departmentId, pageable);
        return toPagedResponse(page);
    }

    public PagedResponse<UserResponseDto> getEmployeesByDepartmentPaged(Long userId, Pageable pageable) {
        User user = repo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (user.getRole() != UserRole.MANAGER) {
            throw new RuntimeException("Access denied. Only managers can view employees in their department.");
        }
        Long departmentId = user.getDepartment().getDepartmentId();
        Page<User> page = repo.findByRoleAndDepartment_DepartmentId(UserRole.EMPLOYEE, departmentId, pageable);
        return toPagedResponse(page);
    }

    private PagedResponse<UserResponseDto> toPagedResponse(Page<User> page) {
        return PagedResponse.<UserResponseDto>builder()
                .content(page.getContent().stream().map(this::mapToResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }

    private UserResponseDto mapToResponse(User user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .departmentId(user.getDepartment().getDepartmentId())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .username(user.getUsername())
                .build();
    }
}
