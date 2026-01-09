package com.zetumall.store;

import com.zetumall.security.SupabaseAuthenticatedUser;
import com.zetumall.shared.ApiResponse;
import com.zetumall.store.dto.StoreCreateRequest;
import com.zetumall.store.dto.StoreResponse;
import com.zetumall.user.User;
import com.zetumall.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@Slf4j
public class StoreController {

    private final StoreService storeService;
    private final UserRepository userRepository;

    /**
     * Create a new store
     * POST /api/stores
     */
    @PostMapping
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(
            @RequestBody StoreCreateRequest request,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            log.info("Creating store for user: {} ({})", authUser.getEmail(), authUser.getId());
            
            Store store = storeService.createStore(request, authUser);
            StoreResponse response = StoreResponse.fromEntity(store);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Store created successfully!"));
                    
        } catch (RuntimeException e) {
            log.error("Store creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during store creation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create store. Please try again."));
        }
    }

    /**
     * Get current user's store
     * GET /api/stores/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<StoreResponse>> getMyStore(
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            // Get user from database
            User user = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Store store = storeService.getStoreByUserId(user.getId());
            StoreResponse response = StoreResponse.fromEntity(store);
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (RuntimeException e) {
            log.error("Store not found for user: {}", authUser.getId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Store not found"));
        } catch (Exception e) {
            log.error("Error fetching user store", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch store"));
        }
    }

    /**
     * Get store by ID
     * GET /api/stores/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StoreResponse>> getStoreById(@PathVariable String id) {
        try {
            Store store = storeService.getStoreById(id);
            StoreResponse response = StoreResponse.fromEntity(store);
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Store not found"));
        }
    }

    /**
     * Update store
     * PUT /api/stores/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStore(
            @PathVariable String id,
            @RequestBody StoreCreateRequest request,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            // Get user from database
            User user = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Store store = storeService.updateStore(id, request, user.getId());
            StoreResponse response = StoreResponse.fromEntity(store);
            
            return ResponseEntity.ok(ApiResponse.success(response, "Store updated successfully"));
            
        } catch (RuntimeException e) {
            log.error("Store update failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during store update", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update store"));
        }
    }

    /**
     * List all stores (public/admin)
     * GET /api/stores
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getAllStores(
            @RequestParam(required = false) String status
    ) {
        try {
            List<Store> stores;
            
            if (status != null) {
                Store.StoreStatus storeStatus = Store.StoreStatus.valueOf(status.toUpperCase());
                stores = storeService.getStoresByStatus(storeStatus);
            } else {
                stores = storeService.getAllStores();
            }
            
            List<StoreResponse> responses = stores.stream()
                    .map(StoreResponse::fromEntity)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(responses));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid status value"));
        } catch (Exception e) {
            log.error("Error fetching stores", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch stores"));
        }
    }
}
