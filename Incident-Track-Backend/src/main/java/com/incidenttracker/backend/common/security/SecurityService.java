package com.incidenttracker.backend.common.security;

import com.incidenttracker.backend.user.entity.User;
import com.incidenttracker.backend.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecurityService {

    private final UserRepository userRepository;

    public SecurityService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get the currently authenticated User entity.
     * Accessible from any module that injects this service.
     */
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        // String username = authentication.getName();
        String email = authentication.getName();
        return userRepository.findByEmail(email);
        // return userRepository.findByUsername(username);
    }

    /**
     * Get only the username of the logged-in user.
     */
    public String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}

/*
 * Why this is the best way:
 * Dry (Don't Repeat Yourself): You write the security logic once.
 * Consistency: Every module retrieves the user the exact same way.
 * Testability: You can easily mock SecurityService in your unit tests for other
 * modules without needing a full Security Context.
 * 
 * 
 * private final SecurityService securityService;
 * 
 * User currentUser = securityService.getCurrentUser()
 * .orElseThrow(() -> new RuntimeException("Authentication required"));
 */