package com.incidenttracker.backend.user.dto;

import com.incidenttracker.backend.common.enums.UserRole;
import com.incidenttracker.backend.common.enums.UserStatus;
import lombok.Data;

@Data
public class UserRegistrationDTO {
    private String username;
    private String email;
    private String password;
    private Long departmentId; // This matches the "1" in your JSON
    private UserRole role;
    private UserStatus status;
}