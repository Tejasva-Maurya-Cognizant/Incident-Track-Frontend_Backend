package com.incidenttracker.backend.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUser {
    private String username;
    private String password;
}