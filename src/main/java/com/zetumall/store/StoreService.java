package com.zetumall.store;

import com.zetumall.security.SupabaseAuthenticatedUser;
import com.zetumall.store.dto.StoreCreateRequest;
import com.zetumall.user.User;
import com.zetumall.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    /**
     * Create a new store
     */
    @Transactional
    public Store createStore(StoreCreateRequest request, SupabaseAuthenticatedUser authUser) {
        // Find or create user in database
        User user = userRepository.findByAuthId(authUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found. Please sync your account."));

        // Check for role restriction
        if (user.getRole() == User.Role.ADMIN) {
            throw new RuntimeException("Admins cannot create stores. Please create a separate Seller account.");
        }

        // Check for existing store
        storeRepository.findByUserId(user.getId()).ifPresent(existingStore -> {
            if (existingStore.getStatus() != Store.StoreStatus.PENDING) {
                throw new RuntimeException("You already have an active store");
            }
        });

        // Check username availability
        if (storeRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken. Please choose another.");
        }

        // Create custom theme JSON
        String customTheme = String.format("{\"category\":\"%s\",\"tagline\":\"%s\"}",
                request.getCategory() != null ? request.getCategory() : "",
                request.getTagline() != null ? request.getTagline() : "");

        // Build store entity
        Store store = new Store();
        store.setUserId(user.getId());
        store.setName(request.getName());
        store.setUsername(request.getUsername());
        store.setDescription(request.getDescription());
        store.setEmail(request.getEmail() != null ? request.getEmail() : user.getEmail());
        store.setContact(request.getContact() != null ? request.getContact() : "");
        store.setAddress(request.getAddress() != null ? request.getAddress() : "Not specified");
        store.setLogo(request.getLogo() != null ? request.getLogo() : "");
        store.setCustomBanner(request.getCustomBanner());
        store.setShippingPolicy(request.getShippingPolicy());
        store.setReturnPolicy(request.getReturnPolicy());
        store.setCompliancePolicy(request.getCompliancePolicy());
        store.setBannerPrompt(request.getBannerPrompt());
        store.setCustomTheme(customTheme);
        store.setMpesaNumber(request.getMpesaNumber());
        store.setPaymentMethods(request.getPaymentInfo() != null ? request.getPaymentInfo() : "[]");
        
        // Set defaults and auto-approval for MVP
        store.setStatus(Store.StoreStatus.APPROVED);  // Auto-approve for now
        store.setIsActive(true);
        store.setSubscriptionExpiresAt(LocalDateTime.now().plusMonths(6));  // 6 months free trial

        // Update user role to SUPPLIER
        if (user.getRole() != User.Role.SUPPLIER) {
            user.setRole(User.Role.SUPPLIER);
            userRepository.save(user);
        }

        // Save store
        Store savedStore = storeRepository.save(store);
        
        log.info("Store created successfully: {} by user: {}", savedStore.getId(), user.getId());
        
        return savedStore;
    }

    /**
     * Get store by user ID
     */
    public Store getStoreByUserId(String userId) {
        return storeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Store not found"));
    }

    /**
     * Get store by ID
     */
    public Store getStoreById(String storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));
    }

    /**
     * Update store
     */
    @Transactional
    public Store updateStore(String storeId, StoreCreateRequest request, String userId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        // Verify ownership
        if (!store.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to update this store");
        }

        // Update fields
        if (request.getName() != null) store.setName(request.getName());
        if (request.getDescription() != null) store.setDescription(request.getDescription());
        if (request.getEmail() != null) store.setEmail(request.getEmail());
        if (request.getContact() != null) store.setContact(request.getContact());
        if (request.getAddress() != null) store.setAddress(request.getAddress());
        if (request.getLogo() != null) store.setLogo(request.getLogo());
        if (request.getCustomBanner() != null) store.setCustomBanner(request.getCustomBanner());
        if (request.getShippingPolicy() != null) store.setShippingPolicy(request.getShippingPolicy());
        if (request.getReturnPolicy() != null) store.setReturnPolicy(request.getReturnPolicy());
        if (request.getMpesaNumber() != null) store.setMpesaNumber(request.getMpesaNumber());

        return storeRepository.save(store);
    }

    /**
     * Get all stores (for admin/public listing)
     */
    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    /**
     * Get stores by status
     */
    public List<Store> getStoresByStatus(Store.StoreStatus status) {
        return storeRepository.findByStatus(status);
    }
}
