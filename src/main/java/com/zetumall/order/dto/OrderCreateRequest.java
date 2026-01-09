package com.zetumall.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {
    private String storeId;
    private List<OrderItemRequest> items;
    private String shippingAddress;
    private String shippingPhone;
    private String paymentMethod;
    private String notes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        private String productId;
        private Integer quantity;
    }
}
