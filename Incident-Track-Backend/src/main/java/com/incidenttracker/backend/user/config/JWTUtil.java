package com.incidenttracker.backend.user.config;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

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

    public String generateToken(String username) {
        return Jwts.builder() // Starts building the JWT token.
                // Sets the subject of the token to the provided username.
                .subject(username)

                // Sets the token's issue time to the current date and time.
                .issuedAt(new Date())

                // 10 hours, Sets the token's expiration time to 10 hours from now.
                .expiration(new Date(System.currentTimeMillis() + 36000000))

                // Signs the token using the secret key and HMAC SHA-256 algorithm.
                .signWith(getSigningKey())

                // Finalizes and returns the compact JWT string.
                .compact();
    }

    // Method to extract the username (subject) from a JWT token.
    public String extractUsername(String token) {

        // Begins building the JWT parser.
        return Jwts.parser()

                .verifyWith(getSigningKey()) // .setSigningKey() is now .verifyWith()
                .build()
                .parseSignedClaims(token) // .parseClaimsJws() is now .parseSignedClaims()
                .getPayload() // .getBody() is now .getPayload()
                .getSubject();
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

}
