package com.zetumall.escrow;

import com.zetumall.security.SupabaseAuthenticatedUser;
import com.zetumall.shared.ApiResponse;
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

@RestController
@RequestMapping("/api/escrow")
@RequiredArgsConstructor
@Slf4j
public class EscrowController {

    private final EscrowService escrowService;
    private final UserRepository userRepository;

    /**
     * Get all escrow transactions (FINANCE_ADMIN, SUPER_ADMIN)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('FINANCE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<EscrowTransaction>>> getAllTransactions() {
        return ResponseEntity.ok(ApiResponse.success(escrowService.getAllTransactions()));
    }

    /**
     * Release Funds (FINANCE_ADMIN, SUPER_ADMIN)
     * POST /api/escrow/{id}/release
     */
    @PostMapping("/{id}/release")
    @PreAuthorize("hasAnyRole('FINANCE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EscrowTransaction>> releaseFunds(
            @PathVariable String id,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser) {
        try {
            User admin = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            EscrowTransaction tx = escrowService.releaseEscrow(id, admin.getId());
            return ResponseEntity.ok(ApiResponse.success(tx, "Funds released to seller"));
        } catch (Exception e) {
            log.error("Release failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Refund Funds (FINANCE_ADMIN, SUPER_ADMIN)
     * POST /api/escrow/{id}/refund
     */
    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('FINANCE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EscrowTransaction>> refundFunds(
            @PathVariable String id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser) {
        try {
            User admin = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            String reason = request.getOrDefault("reason", "Administrative Refund");
            EscrowTransaction tx = escrowService.refundEscrow(id, reason, admin.getId());
            return ResponseEntity.ok(ApiResponse.success(tx, "Funds refunded to buyer"));
        } catch (Exception e) {
            log.error("Refund failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Initialse Escrow (Test Endpoint for now, usually internal)
     */
    @PostMapping("/initiate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EscrowTransaction>> initiateTest(
            @RequestBody Map<String, String> request) {
        try {
            String orderId = request.get("orderId");
            EscrowTransaction tx = escrowService.initiateEscrow(orderId, "TEST_METHOD", "TEST_REF");
            return ResponseEntity.ok(ApiResponse.success(tx));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }
}
