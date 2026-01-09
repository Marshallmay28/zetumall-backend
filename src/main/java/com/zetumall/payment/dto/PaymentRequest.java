package com.zetumall.payment.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private String orderId;
    private String phoneNumber; // Format: 254...
    private Double amount;
}
