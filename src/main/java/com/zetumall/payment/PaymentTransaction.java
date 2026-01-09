package com.zetumall.payment;

import com.zetumall.order.Order;
import com.zetumall.shared.BaseEntity;
import com.zetumall.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "\"PaymentTransaction\"", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "merchant_request_id")
    private String merchantRequestId;

    @Column(name = "checkout_request_id", unique = true)
    private String checkoutRequestId;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "receipt_number")
    private String receiptNumber;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "transaction_date")
    private String transactionDate; // Format from M-Pesa

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, CANCELLED
    }
}
