package com.zetumall.user;

import com.zetumall.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "\"User\"", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Column(name = "auth_id")
    private String authId; // Supabase Auth UUID

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(name = "image")
    private String image;

    @Column(nullable = false)
    private String password;

    @Column(columnDefinition = "jsonb", nullable = false)
    private String cart = "{}";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.BUYER;

    @Column
    private String country = "KEN";

    @Column
    private String currency = "KES";

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "ban_reason")
    private String banReason;

    @Column(name = "reset_token", unique = true)
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private java.time.LocalDateTime resetTokenExpiry;

    @Column(name = "seller_rating", precision = 3, scale = 2)
    private BigDecimal sellerRating;

    @Column(name = "total_sales", nullable = false)
    private Integer totalSales = 0;

    @Column(name = "successful_deliveries", nullable = false)
    private Integer successfulDeliveries = 0;

    @Column(name = "disputes_lost", nullable = false)
    private Integer disputesLost = 0;

    @Column(name = "is_trusted_seller", nullable = false)
    private Boolean isTrustedSeller = false;

    public enum Role {
        BUYER,
        SUPPLIER,
        ADMIN, // Deprecated, migrate to specific roles
        SUPER_ADMIN,
        FINANCE_ADMIN,
        SECURITY_ADMIN,
        OPERATIONS_ADMIN,
        SUPPORT_ADMIN
    }
}
