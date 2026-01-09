package com.zetumall.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreCreateRequest {
    private String name;
    private String username;
    private String description;
    private String email;
    private String contact;
    private String address;
    private String category;
    private String logo;
    private String customBanner;
    
    // Template-specific fields
    private String tagline;
    private String shippingPolicy;
    private String returnPolicy;
    private String compliancePolicy;
    private String bannerPrompt;
    
    // Payment info
    private String mpesaNumber;
    private String paymentInfo;  // JSON string of payment methods
}
