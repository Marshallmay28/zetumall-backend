package com.zetumall.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthTestController {

    /**
     * Test endpoint to verify authentication
     * Requires any authenticated user
     */
    @GetMapping("/auth/test")
    public Map<String, Object> testAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", auth != null && auth.isAuthenticated());

        if (auth != null) {
            response.put("principal", auth.getPrincipal());
            response.put("authorities", auth.getAuthorities());
            response.put("name", auth.getName());
        }

        return response;
    }

    /**
     * Admin-only test endpoint
     * Requires admin role
     */
    @GetMapping("/auth/admin-test")
    public Map<String, Object> testAdminAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "You have admin access!");
        response.put("principal", auth.getPrincipal());
        response.put("authorities", auth.getAuthorities());

        return response;
    }
}
