package com.zetumall.order;

import com.zetumall.order.dto.OrderCreateRequest;
import com.zetumall.order.dto.OrderResponse;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    /**
     * Create a new order
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @RequestBody OrderCreateRequest request,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser) {
        try {
            User user = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Order order = orderService.createOrder(request, user.getId());
            OrderResponse response = OrderResponse.fromEntity(order, orderService.getEscrowByOrderId(order.getId()));

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Order created successfully with escrow protection"));

        } catch (RuntimeException e) {
            log.error("Order creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during order creation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create order"));
        }
    }

    /**
     * Get order by ID
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable String id) {
        try {
            Order order = orderService.getOrderById(id);
            OrderResponse response = OrderResponse.fromEntity(order, orderService.getEscrowByOrderId(order.getId()));

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Order not found"));
        }
    }

    /**
     * Get my orders (as buyer)
     * GET /api/orders/my-orders
     */
    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser) {
        try {
            User user = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Order> orders = orderService.getBuyerOrders(user.getId());
            List<OrderResponse> responses = orders.stream()
                    .map(order -> OrderResponse.fromEntity(order, orderService.getEscrowByOrderId(order.getId())))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(responses));

        } catch (Exception e) {
            log.error("Error fetching buyer orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch orders"));
        }
    }

    /**
     * Get store orders (as seller)
     * GET /api/orders/store/{storeId}
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getStoreOrders(
            @PathVariable String storeId,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser) {
        try {
            User user = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Order> orders = orderService.getStoreOrders(storeId, user.getId());
            List<OrderResponse> responses = orders.stream()
                    .map(order -> OrderResponse.fromEntity(order, orderService.getEscrowByOrderId(order.getId())))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(responses));

        } catch (RuntimeException e) {
            log.error("Error fetching store orders: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching store orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch store orders"));
        }
    }

    /**
     * Update order status
     * PUT /api/orders/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser) {
        try {
            User user = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String statusStr = request.get("status");
            Order.OrderStatus status = Order.OrderStatus.valueOf(statusStr.toUpperCase());

            Order order = orderService.updateOrderStatus(id, status, user.getId());
            OrderResponse response = OrderResponse.fromEntity(order, orderService.getEscrowByOrderId(order.getId()));

            return ResponseEntity.ok(ApiResponse.success(response, "Order status updated"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid status value"));
        } catch (RuntimeException e) {
            log.error("Order status update failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error updating order status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update order status"));
        }
    }

    /**
     * Release escrow with release code
     * POST /api/orders/{id}/release-escrow
     */
    @PostMapping("/{id}/release-escrow")
    public ResponseEntity<ApiResponse<String>> releaseEscrow(
            @PathVariable String id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser) {
        try {
            User user = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String releaseCode = request.get("releaseCode");
            if (releaseCode == null || releaseCode.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Release code is required"));
            }

            orderService.releaseEscrow(id, releaseCode, user.getId());

            return ResponseEntity.ok(ApiResponse.success("Escrow funds released successfully to seller"));

        } catch (RuntimeException e) {
            log.error("Escrow release failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error releasing escrow", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to release escrow"));
        }
    }
}
