package com.zetumall.health;

import com.zetumall.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "zetumall-core-api");
        health.put("version", "1.0.0");
        health.put("timestamp", System.currentTimeMillis());

        // Database connectivity check
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            health.put("database", "connected");
        } catch (Exception e) {
            health.put("database", "disconnected");
            health.put("error", e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.success(health, "Health check successful"));
    }
}
