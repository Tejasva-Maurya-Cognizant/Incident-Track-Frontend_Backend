package com.incidenttracker.backend.user.config;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

@Component
public class JWTUtil {
    String SECRET_STRING = "123456789012345678901234567890123456";

    private final SecretKey getSigningKey() {
        byte[] keyBytes = SECRET_STRING.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /** Original method kept for backward compatibility */
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 36000000))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * New overload: embeds role + userId claims so the filter and services never
     * need a DB call
     */
    public String generateToken(String username, String role, Long userId) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 36000000))
                .signWith(getSigningKey())
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Method to extract the username (subject) from a JWT token.
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Returns the role claim embedded in the token, or null if absent (old tokens).
     */
    public String extractRole(String token) {
        try {
            return extractAllClaims(token).get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the userId claim embedded in the token, or null if absent (old
     * tokens).
     */
    public Long extractUserId(String token) {
        try {
            return extractAllClaims(token).get("userId", Long.class);
        } catch (Exception e) {
            return null;
        }
    }

    // Method to validate the token by comparing its username with the expected
    // user.
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate token using only JWT claims — no UserDetails / no DB call needed.
     */
    public boolean validateToken(String token, String username) {
        try {
            return extractUsername(token).equals(username);
        } catch (Exception e) {
            return false;
        }
    }

}
