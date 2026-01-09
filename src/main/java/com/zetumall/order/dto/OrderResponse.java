package com.zetumall.order.dto;

import com.zetumall.order.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String id;
    private String buyerId;
    private String storeId;
    private String storeName;
    private BigDecimal total;
    private String status;
    private String shippingAddress;
    private String shippingPhone;
    private String paymentMethod;
    private String paymentStatus;
    private String trackingNumber;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Escrow info
    private String escrowStatus;
    private String releaseCode;

    public static OrderResponse fromEntity(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setBuyerId(order.getBuyerId());
        response.setStoreId(order.getStoreId());
        response.setTotal(order.getTotal());
        response.setStatus(order.getStatus().name());
        response.setShippingAddress(order.getShippingAddress());
        response.setShippingPhone(order.getShippingPhone());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setTrackingNumber(order.getTrackingNumber());
        response.setShippedAt(order.getShippedAt());
        response.setDeliveredAt(order.getDeliveredAt());
        response.setNotes(order.getNotes());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        
        if (order.getStore() != null) {
            response.setStoreName(order.getStore().getName());
        }
        
        return response;
    }
}
