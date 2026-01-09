package com.zetumall.store.dto;

import com.zetumall.store.Store;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponse {
    private String id;
    private String userId;
    private String name;
    private String description;
    private String username;
    private String address;
    private String country;
    private String status;
    private Boolean isActive;
    private String logo;
    private String email;
    private String contact;
    private String subscriptionPlan;
    private LocalDateTime subscriptionExpiresAt;
    private Double trustScore;
    private String trustLevel;
    private String customBanner;
    private String customTheme;
    private String mpesaNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StoreResponse fromEntity(Store store) {
        StoreResponse response = new StoreResponse();
        response.setId(store.getId());
        response.setUserId(store.getUserId());
        response.setName(store.getName());
        response.setDescription(store.getDescription());
        response.setUsername(store.getUsername());
        response.setAddress(store.getAddress());
        response.setCountry(store.getCountry());
        response.setStatus(store.getStatus().name());
        response.setIsActive(store.getIsActive());
        response.setLogo(store.getLogo());
        response.setEmail(store.getEmail());
        response.setContact(store.getContact());
        response.setSubscriptionPlan(store.getSubscriptionPlan().name());
        response.setSubscriptionExpiresAt(store.getSubscriptionExpiresAt());
        response.setTrustScore(store.getTrustScore());
        response.setTrustLevel(store.getTrustLevel());
        response.setCustomBanner(store.getCustomBanner());
        response.setCustomTheme(store.getCustomTheme());
        response.setMpesaNumber(store.getMpesaNumber());
        response.setCreatedAt(store.getCreatedAt());
        response.setUpdatedAt(store.getUpdatedAt());
        return response;
    }
}
