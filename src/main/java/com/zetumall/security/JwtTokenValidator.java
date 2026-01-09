package com.zetumall.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.zetumall.config.SupabaseProperties;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenValidator {

    private final SupabaseProperties supabaseProperties;

    /**
     * Validate Supabase JWT token
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Parse JWT token and extract claims
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(supabaseProperties.getJwtSecret().getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extract user ID (sub claim) from token
     */
    public String getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * Extract user email from token
     */
    public String getEmailFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("email", String.class);
    }

    /**
     * Extract user role from token
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }
}
