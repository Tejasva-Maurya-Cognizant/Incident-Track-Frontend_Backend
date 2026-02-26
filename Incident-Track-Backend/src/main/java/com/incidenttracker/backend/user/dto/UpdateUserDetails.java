package com.incidenttracker.backend.user.dto;

import com.incidenttracker.backend.common.enums.UserRole;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDetails {
    private String username;
    private String department;
    private UserRole role;
    private String password;
    private String email;
    private Long departmentId;
}