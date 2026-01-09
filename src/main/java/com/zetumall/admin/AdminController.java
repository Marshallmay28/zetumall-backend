package com.zetumall.admin;

import com.zetumall.product.Product;
import com.zetumall.product.dto.ProductResponse;
import com.zetumall.security.SupabaseAuthenticatedUser;
import com.zetumall.shared.ApiResponse;
import com.zetumall.store.Store;
import com.zetumall.store.dto.StoreResponse;
import com.zetumall.user.User;
import com.zetumall.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;
    private final UserRepository userRepository;

    /**
     * Get platform statistics
     * GET /api/admin/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPlatformStats() {
        try {
            Map<String, Object> stats = adminService.getPlatformStats();
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("Error fetching platform stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch statistics"));
        }
    }

    /**
     * Get pending stores for approval
     * GET /api/admin/stores/pending
     */
    @GetMapping("/stores/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getPendingStores() {
        try {
            List<Store> stores = adminService.getPendingStores();
            List<StoreResponse> responses = stores.stream()
                    .map(StoreResponse::fromEntity)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("Error fetching pending stores", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch pending stores"));
        }
    }

    /**
     * Approve a store
     * POST /api/admin/stores/{id}/approve
     */
    @PostMapping("/stores/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StoreResponse>> approveStore(
            @PathVariable String id,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            User admin = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            
            Store store = adminService.approveStore(id, admin.getId());
            StoreResponse response = StoreResponse.fromEntity(store);
            
            return ResponseEntity.ok(ApiResponse.success(response, "Store approved successfully"));
        } catch (RuntimeException e) {
            log.error("Store approval failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error approving store", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to approve store"));
        }
    }

    /**
     * Reject a store
     * POST /api/admin/stores/{id}/reject
     */
    @PostMapping("/stores/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StoreResponse>> rejectStore(
            @PathVariable String id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            User admin = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            
            String reason = request.get("reason");
            if (reason == null || reason.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Rejection reason is required"));
            }
            
            Store store = adminService.rejectStore(id, reason, admin.getId());
            StoreResponse response = StoreResponse.fromEntity(store);
            
            return ResponseEntity.ok(ApiResponse.success(response, "Store rejected"));
        } catch (RuntimeException e) {
            log.error("Store rejection failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error rejecting store", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to reject store"));
        }
    }

    /**
     * Suspend a store
     * POST /api/admin/stores/{id}/suspend
     */
    @PostMapping("/stores/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StoreResponse>> suspendStore(
            @PathVariable String id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            User admin = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            
            String reason = request.get("reason");
            Store store = adminService.suspendStore(id, reason, admin.getId());
            StoreResponse response = StoreResponse.fromEntity(store);
            
            return ResponseEntity.ok(ApiResponse.success(response, "Store suspended"));
        } catch (RuntimeException e) {
            log.error("Store suspension failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error suspending store", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to suspend store"));
        }
    }

    /**
     * Get all users
     * GET /api/admin/users
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllUsers() {
        try {
            List<User> users = adminService.getAllUsers();
            List<Map<String, Object>> userResponses = users.stream()
                    .map(user -> Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "role", user.getRole().name(),
                        "isActive", user.getIsActive(),
                        "banReason", user.getBanReason() != null ? user.getBanReason() : "",
                        "createdAt", user.getCreatedAt()
                    ))
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(userResponses));
        } catch (Exception e) {
            log.error("Error fetching users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch users"));
        }
    }

    /**
     * Ban/unban a user
     * POST /api/admin/users/{id}/ban
     */
    @PostMapping("/users/{id}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> toggleUserBan(
            @PathVariable String id,
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            User admin = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            
            Boolean ban = (Boolean) request.get("ban");
            String reason = (String) request.get("reason");
            
            adminService.toggleUserBan(id, ban, reason, admin.getId());
            
            return ResponseEntity.ok(ApiResponse.success(
                ban ? "User banned successfully" : "User unbanned successfully"
            ));
        } catch (RuntimeException e) {
            log.error("User ban toggle failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error toggling user ban", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update user status"));
        }
    }

    /**
     * Moderate a product
     * POST /api/admin/products/{id}/moderate
     */
    @PostMapping("/products/{id}/moderate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> moderateProduct(
            @PathVariable String id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            User admin = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            
            String statusStr = request.get("status");
            Product.ProductStatus status = Product.ProductStatus.valueOf(statusStr.toUpperCase());
            
            Product product = adminService.moderateProduct(id, status, admin.getId());
            ProductResponse response = ProductResponse.fromEntity(product);
            
            return ResponseEntity.ok(ApiResponse.success(response, "Product moderated"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid status value"));
        } catch (RuntimeException e) {
            log.error("Product moderation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error moderating product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to moderate product"));
        }
    }
}
