package com.incidenttracker.backend.common.security;

import com.incidenttracker.backend.user.config.JWTUtil;
import com.incidenttracker.backend.user.entity.User;
import com.incidenttracker.backend.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Service
public class SecurityService {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    public SecurityService(UserRepository userRepository, JWTUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Fast path: read userId directly from the JWT claim — zero DB calls.
     * Falls back to null if the token is old (no userId claim) or not present.
     */
    public Long getCurrentUserIdFromToken() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null)
                return null;
            HttpServletRequest request = attrs.getRequest();
            String token = null;
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            } else {
                String queryToken = request.getParameter("token");
                if (queryToken != null && !queryToken.isBlank())
                    token = queryToken;
            }
            return token != null ? jwtUtil.extractUserId(token) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the currently authenticated User entity (hits DB).
     * Use getCurrentUserIdFromToken() when you only need the ID.
     */
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email);
    }

    /**
     * Get only the username (email) of the logged-in user — no DB call.
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