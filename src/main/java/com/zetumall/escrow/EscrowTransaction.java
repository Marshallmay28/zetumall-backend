package com.zetumall.escrow;

import com.zetumall.order.Order;
import com.zetumall.shared.BaseEntity;
import com.zetumall.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "\"EscrowTransaction\"", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EscrowTransaction extends BaseEntity {

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    @Column(name = "buyer_id", nullable = false)
    private String buyerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", insertable = false, updatable = false)
    private User buyer;

    @Column(name = "seller_id", nullable = false)
    private String sellerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", insertable = false, updatable = false)
    private User seller;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "platform_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal platformFee = BigDecimal.ZERO;

    @Column(name = "seller_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellerAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EscrowStatus status = EscrowStatus.HELD;

    @Column(name = "held_at", nullable = false)
    private LocalDateTime heldAt = LocalDateTime.now();

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "payment_ref")
    private String paymentRef;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "release_code")
    private String releaseCode;  // Simplified - store generated code

    public enum EscrowStatus {
        HELD, RELEASED, REFUNDED, DISPUTED, EXPIRED
    }
}
