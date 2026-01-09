package com.zetumall.payment;

import com.zetumall.payment.dto.MpesaCallback;
import com.zetumall.payment.dto.PaymentRequest;
import com.zetumall.security.SupabaseAuthenticatedUser;
import com.zetumall.shared.ApiResponse;
import com.zetumall.user.User;
import com.zetumall.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    /**
     * Initiate M-Pesa STK Push
     * POST /api/payments/mpesa/stk-push
     */
    @PostMapping("/mpesa/stk-push")
    public ResponseEntity<ApiResponse<PaymentTransaction>> initiatePayment(
            @RequestBody PaymentRequest request,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            User user = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            PaymentTransaction transaction = paymentService.initiatePayment(request, user.getId());

            return ResponseEntity.ok(ApiResponse.success(transaction, "Payment initiated. Check your phone."));

        } catch (Exception e) {
            log.error("Error initiating payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to initiate payment: " + e.getMessage()));
        }
    }

    /**
     * M-Pesa Callback Webhook
     * POST /api/payments/mpesa/callback
     */
    @PostMapping("/mpesa/callback")
    public ResponseEntity<String> handleCallback(@RequestBody MpesaCallback callback) {
        try {
            paymentService.processCallback(callback);
            // Always return 200/Success content to M-Pesa/Safaricom otherwise they retry
            return ResponseEntity.ok("{\"ResultCode\": 0, \"ResultDesc\": \"Accepted\"}");
        } catch (Exception e) {
            log.error("Error handling callback", e);
            return ResponseEntity.ok("{\"ResultCode\": 0, \"ResultDesc\": \"Accepted\"}"); // Still ack
        }
    }

    /**
     * Check payment status
     * GET /api/payments/{orderId}/status
     */
    @GetMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<PaymentTransaction>> getStatus(
            @PathVariable String orderId,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            PaymentTransaction transaction = paymentService.getStatus(orderId);
            return ResponseEntity.ok(ApiResponse.success(transaction));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Payment status not found"));
        }
    }
}
