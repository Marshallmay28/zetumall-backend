package com.zetumall.product.dto;

import com.zetumall.product.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private Double mrp;
    private Double price;
    private String[] images;
    private String category;
    private Boolean inStock;
    private String storeId;
    private String storeName;
    private Boolean isFeatured;
    private LocalDateTime featuredUntil;
    private Integer viewCount;
    private Integer salesCount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductResponse fromEntity(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setMrp(product.getMrp());
        response.setPrice(product.getPrice());
        response.setImages(product.getImages());
        response.setCategory(product.getCategory());
        response.setInStock(product.getInStock());
        response.setStoreId(product.getStoreId());
        response.setIsFeatured(product.getIsFeatured());
        response.setFeaturedUntil(product.getFeaturedUntil());
        response.setViewCount(product.getViewCount());
        response.setSalesCount(product.getSalesCount());
        response.setStatus(product.getStatus().name());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        
        // Store name if available
        if (product.getStore() != null) {
            response.setStoreName(product.getStore().getName());
        }
        
        return response;
    }
}
