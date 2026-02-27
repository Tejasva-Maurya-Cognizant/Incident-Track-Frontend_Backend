package com.incidenttracker.backend.user.config;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.incidenttracker.backend.user.service.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    JWTUtil jwtUtils;
    @Autowired
    CustomUserDetailsService userDetailsService;

    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        if (path.equals("/auth/login") || path.equals("/auth/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // Fallback: allow token via query param for SSE (EventSource cannot set
        // headers)
        if (token == null) {
            String queryToken = request.getParameter("token");
            if (queryToken != null && !queryToken.isBlank()) {
                token = queryToken;
            }
        }

        if (token != null) {
            try {
                username = jwtUtils.extractUsername(token);
            } catch (Exception e) {
                username = null;
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // --- Fast path: build auth from JWT claims only (zero DB calls) ---
            String roleFromToken = jwtUtils.extractRole(token);
            if (roleFromToken != null && jwtUtils.validateToken(token, username)) {
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleFromToken));
                UserDetails userDetails = new User(username, "", authorities);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                        null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                // --- Fallback for old tokens without role claim (hits DB once) ---
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtUtils.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                            null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
