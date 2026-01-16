package com.zetumall.admin;

import com.zetumall.order.Order;
import com.zetumall.order.OrderRepository;
import com.zetumall.product.Product;
import com.zetumall.product.ProductRepository;
import com.zetumall.store.Store;
import com.zetumall.store.StoreRepository;
import com.zetumall.user.User;
import com.zetumall.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final com.zetumall.admin.audit.AdminAuditRepository adminAuditRepository;
    private final OrderRepository orderRepository;

    /**
     * Get platform analytics/statistics
     */
    public Map<String, Object> getPlatformStats() {
        Map<String, Object> stats = new HashMap<>();

        // User stats
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream()
                .filter(User::getIsActive)
                .count();

        // Store stats
        long totalStores = storeRepository.count();
        long pendingStores = storeRepository.findByStatus(Store.StoreStatus.PENDING).size();
        long activeStores = storeRepository.findAll().stream()
                .filter(Store::getIsActive)
                .count();

        // Product stats
        long totalProducts = productRepository.count();
        long activeProducts = productRepository.findByStatus(Product.ProductStatus.APPROVED).size();

        // Order stats
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.PENDING)
                .count();

        stats.put("users", Map.of(
                "total", totalUsers,
                "active", activeUsers));

        stats.put("stores", Map.of(
                "total", totalStores,
                "pending", pendingStores,
                "active", activeStores));

        stats.put("products", Map.of(
                "total", totalProducts,
                "active", activeProducts));

        stats.put("orders", Map.of(
                "total", totalOrders,
                "pending", pendingOrders));

        return stats;
    }

    /**
     * Approve a store
     */
    @Transactional
    public Store approveStore(String storeId, String adminId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        store.setStatus(Store.StoreStatus.APPROVED);
        store.setIsActive(true);
        store.setAdminOverride(true);
        store.setOverrideBy(adminId);
        store.setOverrideAt(LocalDateTime.now());
        store.setOverrideReason("Approved by admin");

        // Set 6-month free trial
        store.setSubscriptionExpiresAt(LocalDateTime.now().plusMonths(6));

        Store savedStore = storeRepository.save(store);
        log.info("Store approved: {} by admin: {}", storeId, adminId);

        return savedStore;
    }

    /**
     * Reject a store
     */
    @Transactional
    public Store rejectStore(String storeId, String reason, String adminId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        store.setStatus(Store.StoreStatus.REJECTED);
        store.setIsActive(false);
        store.setAdminOverride(true);
        store.setOverrideBy(adminId);
        store.setOverrideAt(LocalDateTime.now());
        store.setOverrideReason(reason);

        Store savedStore = storeRepository.save(store);
        log.info("Store rejected: {} by admin: {} - Reason: {}", storeId, adminId, reason);

        return savedStore;
    }

    /**
     * Suspend a store
     */
    @Transactional
    public Store suspendStore(String storeId, String reason, String adminId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        store.setStatus(Store.StoreStatus.SUSPENDED);
        store.setIsActive(false);
        store.setAdminOverride(true);
        store.setOverrideBy(adminId);
        store.setOverrideAt(LocalDateTime.now());
        store.setOverrideReason(reason);

        Store savedStore = storeRepository.save(store);
        log.info("Store suspended: {} by admin: {} - Reason: {}", storeId, adminId, reason);

        return savedStore;
    }

    /**
     * Get pending stores for approval
     */
    public List<Store> getPendingStores() {
        return storeRepository.findByStatus(Store.StoreStatus.PENDING);
    }

    /**
     * Ban/unban a user
     */
    @Transactional
    public User toggleUserBan(String userId, boolean ban, String reason, String adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsActive(!ban);
        user.setBanReason(ban ? reason : null);

        User savedUser = userRepository.save(user);
        log.info("User {} {} by admin: {}", userId, ban ? "banned" : "unbanned", adminId);

        return savedUser;
    }

    /**
     * Get all users (paginated in production)
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Moderate product (approve/reject)
     */
    @Transactional
    public Product moderateProduct(String productId, Product.ProductStatus status, String adminId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setStatus(status);

        Product savedProduct = productRepository.save(product);
        log.info("Product {} moderated to {} by admin: {}", productId, status, adminId);

        return savedProduct;
    }

    /**
     * Lock an admin account
     */
    @Transactional
    public void lockAdmin(String targetAdminId, String reason, String ipAddress, String superAdminId) {
        User superAdmin = userRepository.findById(superAdminId)
                .orElseThrow(() -> new RuntimeException("Super Admin not found"));
        User targetAdmin = userRepository.findById(targetAdminId)
                .orElseThrow(() -> new RuntimeException("Target Admin not found"));

        if (targetAdmin.getRole() == User.Role.SUPER_ADMIN) {
            throw new SecurityException("Cannot lock another Super Admin");
        }

        // Action
        targetAdmin.setIsActive(false);
        targetAdmin.setBanReason("Locked by Super Admin: " + reason);
        userRepository.save(targetAdmin);

        // Audit
        createAuditLog(superAdmin, targetAdmin,
                com.zetumall.admin.audit.AdminCredentialAudit.AuditActionType.ACCOUNT_LOCK, null, null, reason,
                ipAddress);
    }

    /**
     * Unlock an admin account
     */
    @Transactional
    public void unlockAdmin(String targetAdminId, String reason, String ipAddress, String superAdminId) {
        User superAdmin = userRepository.findById(superAdminId)
                .orElseThrow(() -> new RuntimeException("Super Admin not found"));
        User targetAdmin = userRepository.findById(targetAdminId)
                .orElseThrow(() -> new RuntimeException("Target Admin not found"));

        if (targetAdmin.getRole() == User.Role.SUPER_ADMIN) {
            throw new SecurityException("Cannot action another Super Admin");
        }

        // Action
        targetAdmin.setIsActive(true);
        targetAdmin.setBanReason(null);
        userRepository.save(targetAdmin);

        // Audit
        createAuditLog(superAdmin, targetAdmin,
                com.zetumall.admin.audit.AdminCredentialAudit.AuditActionType.ACCOUNT_UNLOCK, null, null, reason,
                ipAddress);
    }

    /**
     * Update Admin Email (and potentially remove/disable)
     */
    @Transactional
    public void updateAdminEmail(String targetAdminId, String newEmail, String reason, String ipAddress,
            String superAdminId) {
        User superAdmin = userRepository.findById(superAdminId)
                .orElseThrow(() -> new RuntimeException("Super Admin not found"));
        User targetAdmin = userRepository.findById(targetAdminId)
                .orElseThrow(() -> new RuntimeException("Target Admin not found"));

        if (targetAdmin.getRole() == User.Role.SUPER_ADMIN) {
            throw new SecurityException("Cannot change email of another Super Admin");
        }

        String oldEmail = targetAdmin.getEmail();

        // Hash for audit privacy (simple placeholder for now, ideally use SHA-256)
        String oldHash = oldEmail;
        String newHash = newEmail;

        if (newEmail == null || newEmail.isEmpty()) {
            // Email Removal Logic
            targetAdmin.setEmail("removed_" + targetAdminId + "@zetumall.disabled");
            targetAdmin.setIsActive(false);
            targetAdmin.setBanReason("Email removed by Super Admin: " + reason);

            // Audit
            createAuditLog(superAdmin, targetAdmin,
                    com.zetumall.admin.audit.AdminCredentialAudit.AuditActionType.EMAIL_REMOVAL, oldHash, null, reason,
                    ipAddress);
        } else {
            // Email Change Logic
            targetAdmin.setEmail(newEmail);
            // In a real scenario, trigger Supabase email update via Admin API here

            // Audit
            createAuditLog(superAdmin, targetAdmin,
                    com.zetumall.admin.audit.AdminCredentialAudit.AuditActionType.EMAIL_CHANGE, oldHash, newHash,
                    reason, ipAddress);
        }

        userRepository.save(targetAdmin);
    }

    /**
     * Reset Admin Password (Force Reset)
     */
    @Transactional
    public void resetAdminPassword(String targetAdminId, String reason, String ipAddress, String superAdminId) {
        User superAdmin = userRepository.findById(superAdminId)
                .orElseThrow(() -> new RuntimeException("Super Admin not found"));
        User targetAdmin = userRepository.findById(targetAdminId)
                .orElseThrow(() -> new RuntimeException("Target Admin not found"));

        if (targetAdmin.getRole() == User.Role.SUPER_ADMIN) {
            throw new SecurityException("Cannot reset password of another Super Admin");
        }

        // In a real scenario, this would trigger a password reset email via Supabase
        // or set a "resetRequired" flag.
        // For now, we'll log it as a "Force Reset" action.
        targetAdmin.setResetToken("FORCE_RESET_BY_ADMIN");
        userRepository.save(targetAdmin);

        // Audit
        createAuditLog(superAdmin, targetAdmin,
                com.zetumall.admin.audit.AdminCredentialAudit.AuditActionType.PASSWORD_RESET, null, null, reason,
                ipAddress);
    }

    private void createAuditLog(User superAdmin, User targetAdmin,
            com.zetumall.admin.audit.AdminCredentialAudit.AuditActionType action, String oldVal, String newVal,
            String reason, String ip) {
        com.zetumall.admin.audit.AdminCredentialAudit audit = com.zetumall.admin.audit.AdminCredentialAudit.builder()
                .superAdmin(superAdmin)
                .targetAdmin(targetAdmin)
                .actionType(action)
                .oldValueHash(oldVal)
                .newValueHash(newVal)
                .reason(reason)
                .ipAddress(ip)
                .build();
        adminAuditRepository.save(audit);
    }
}
