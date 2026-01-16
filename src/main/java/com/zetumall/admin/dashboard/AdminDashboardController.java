package com.zetumall.admin.dashboard;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/admin/dashboard")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SECURITY_ADMIN', 'OPERATIONS_ADMIN')")
public class AdminDashboardController {

    private final LocalDateTime startTime = LocalDateTime.now();
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

    /**
     * Serve the admin dashboard HTML page
     */
    @GetMapping("")
    public String dashboard() {
        return "admin-dashboard";
    }

    /**
     * Get dashboard overview data
     */
    @GetMapping("/api/overview")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getOverview() {
        Map<String, Object> overview = new HashMap<>();

        // Service info
        overview.put("serviceName", "ZetuMall Backend");
        overview.put("version", "1.0.0");
        overview.put("environment", System.getProperty("spring.profiles.active", "production"));
        overview.put("startTime", startTime);
        overview.put("uptime", calculateUptime());
        overview.put("health", "UP");

        return ResponseEntity.ok(overview);
    }

    /**
     * Get system metrics
     */
    @GetMapping("/api/metrics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // JVM Memory
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax();

        metrics.put("memory", Map.of(
                "used", usedMemory,
                "max", maxMemory));

        // System CPU (simplified)
        double cpuLoad = osBean.getSystemLoadAverage();
        metrics.put("cpuUsage", cpuLoad > 0 ? cpuLoad * 10 : 0); // Rough estimate

        // HTTP Requests (mock for now - would need request counter)
        metrics.put("httpRequests", 0);

        return ResponseEntity.ok(metrics);
    }

    /**
     * Get health check details
     */
    @GetMapping("/api/health")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new HashMap<>();

        health.put("status", "UP");
        health.put("components", Map.of(
                "database", Map.of("status", "UP"),
                "diskSpace", Map.of("status", "UP"),
                "jvm", Map.of("status", "UP")));

        return ResponseEntity.ok(health);
    }

    /**
     * Get recent API activity (mock data for now)
     */
    @GetMapping("/api/activity")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getActivity() {
        List<Map<String, Object>> activity = new ArrayList<>();

        // This would typically come from a logging/monitoring service
        // For now, return mock data
        activity.add(Map.of(
                "timestamp", LocalDateTime.now().minusMinutes(5),
                "endpoint", "/api/products",
                "method", "GET",
                "status", 200,
                "duration", 45));

        activity.add(Map.of(
                "timestamp", LocalDateTime.now().minusMinutes(10),
                "endpoint", "/api/orders",
                "method", "POST",
                "status", 201,
                "duration", 120));

        return ResponseEntity.ok(activity);
    }

    /**
     * Calculate uptime in human-readable format
     */
    private String calculateUptime() {
        long seconds = java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds();
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;

        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }
}
