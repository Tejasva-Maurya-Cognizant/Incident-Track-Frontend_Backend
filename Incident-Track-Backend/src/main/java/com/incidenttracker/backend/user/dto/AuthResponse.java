package com.incidenttracker.backend.user.dto;

import com.incidenttracker.backend.common.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class AuthResponse {
    String token;
    String username;
    String email;
    Long userId;
    UserRole role;

    // AuthResponse() {
    // }

    // public AuthResponse(String token) {
    // this.token = token;
    // }

    // public String getToken() {
    // return token;
    // }

    // public void setToken(String token) {
    // this.token = token;
    // }

}
