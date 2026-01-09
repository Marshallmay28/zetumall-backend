package com.zetumall.store;

import com.zetumall.shared.BaseEntity;
import com.zetumall.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"Store\"", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Store extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String address;

    @Column
    private String country = "KEN";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreStatus status = StoreStatus.PENDING;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @Column(nullable = false)
    private String logo;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String contact;

    @Column(name = "commission_rate", nullable = false)
    private Double commissionRate = 10.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false)
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.FREE;

    @Column(name = "subscription_started_at", nullable = false)
    private LocalDateTime subscriptionStartedAt = LocalDateTime.now();

    @Column(name = "subscription_expires_at")
    private LocalDateTime subscriptionExpiresAt;

    @Column(name = "listing_fee_per_product", nullable = false)
    private Double listingFeePerProduct = 0.0;

    @Column(name = "trust_score", nullable = false)
    private Double trustScore = 50.0;

    @Column(name = "trust_level", nullable = false)
    private String trustLevel = "New Seller";

    @Column(name = "risk_level", nullable = false)
    private String riskLevel = "Low";

    @Column(name = "requires_review", nullable = false)
    private Boolean requiresReview = false;

    // AI Generation Metadata
    @Column(name = "ai_generated", nullable = false)
    private Boolean aiGenerated = false;

    @Column(name = "generation_language")
    private String generationLanguage = "en";

    @Column(name = "banner_prompt", columnDefinition = "text")
    private String bannerPrompt;

    // Store Policies
    @Column(name = "shipping_policy", columnDefinition = "text")
    private String shippingPolicy;

    @Column(name = "return_policy", columnDefinition = "text")
    private String returnPolicy;

    @Column(name = "compliance_policy", columnDefinition = "text")
    private String compliancePolicy;

    @Column(name = "refund_policy", columnDefinition = "text")
    private String refundPolicy;

    @Column(name = "privacy_policy", columnDefinition = "text")
    private String privacyPolicy;

    @Column(name = "terms_of_service", columnDefinition = "text")
    private String termsOfService;

    // Admin Override
    @Column(name = "admin_override", nullable = false)
    private Boolean adminOverride = false;

    @Column(name = "override_reason")
    private String overrideReason;

    @Column(name = "override_by")
    private String overrideBy;

    @Column(name = "override_at")
    private LocalDateTime overrideAt;

    // Customization
    @Column(name = "custom_banner")
    private String customBanner;

    @Column(name = "custom_theme", columnDefinition = "jsonb", nullable = false)
    private String customTheme = "{}";

    @Column(name = "custom_domain")
    private String customDomain;

    // Payment
    @Column(name = "mpesa_number")
    private String mpesaNumber;

    @Column(name = "payment_methods", columnDefinition = "jsonb", nullable = false)
    private String paymentMethods = "[]";

    // AI Usage Tracking
    @Column(name = "ai_usage_count", nullable = false)
    private Integer aiUsageCount = 0;

    @Column(name = "ai_usage_reset_date", nullable = false)
    private LocalDateTime aiUsageResetDate = LocalDateTime.now();

    public enum StoreStatus {
        PENDING, APPROVED, REJECTED, SUSPENDED
    }

    public enum SubscriptionPlan {
        FREE, BASIC, PREMIUM, ENTERPRISE
    }
}
