package com.zetumall.order;

import com.zetumall.shared.BaseEntity;
import com.zetumall.store.Store;
import com.zetumall.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "\"Order\"", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

    @Column(name = "buyer_id", nullable = false)
    private String buyerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", insertable = false, updatable = false)
    private User buyer;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", insertable = false, updatable = false)
    private Store store;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;

    @Column(name = "shipping_phone", nullable = false)
    private String shippingPhone;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus = "PENDING";

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "shipped_at")
    private java.time.LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private java.time.LocalDateTime deliveredAt;

    @Column(columnDefinition = "text")
    private String notes;

    public enum OrderStatus {
        PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, DISPUTED
    }
}
