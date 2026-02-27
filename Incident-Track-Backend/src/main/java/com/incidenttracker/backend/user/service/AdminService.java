package com.incidenttracker.backend.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.incidenttracker.backend.common.enums.UserStatus;
import com.incidenttracker.backend.department.entity.Department;
import com.incidenttracker.backend.department.repository.DepartmentRepository;
import com.incidenttracker.backend.user.dto.UpdateUserDetails;
import com.incidenttracker.backend.user.entity.User;
import com.incidenttracker.backend.user.repository.UserRepository;

@Service
public class AdminService {
    @Autowired
    UserRepository userRepo;
    @Autowired
    DepartmentRepository departmentRepo;

    public ResponseEntity<String> toggleUserStatus(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        UserStatus newStatus = user.getStatus() == UserStatus.ACTIVE ? UserStatus.INACTIVE : UserStatus.ACTIVE;
        user.setStatus(newStatus);
        userRepo.save(user);
        return ResponseEntity.ok("User status toggled to " + newStatus);
    }

    public ResponseEntity<String> updateUserDetails(Long id, UpdateUserDetails updateObj) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return userRepo.findById(id)
                .map(user -> {
                    if (updateObj.getUsername() != null)
                        user.setUsername(updateObj.getUsername());
                    if (updateObj.getPassword() != null)
                        user.setPassword(passwordEncoder.encode(updateObj.getPassword()));
                    if (updateObj.getEmail() != null)
                        user.setEmail(updateObj.getEmail());
                    if (updateObj.getRole() != null)
                        user.setRole(updateObj.getRole());

                    if (updateObj.getDepartmentId() != null) {
                        Department dept = departmentRepo.findById(updateObj.getDepartmentId())
                                .orElseThrow(() -> new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST, "Invalid departmentId"));
                        user.setDepartment(dept);
                    }

                    userRepo.save(user);
                    return ResponseEntity.ok("User with id " + id + " updated successfully");
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found with id " + id));
    }
}
