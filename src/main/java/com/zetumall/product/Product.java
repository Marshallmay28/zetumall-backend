package com.zetumall.product;

import com.zetumall.shared.BaseEntity;
import com.zetumall.store.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"Product\"", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private Double mrp;  // Maximum Retail Price

    @Column(nullable = false)
    private Double price;  // Selling price

    @Column(columnDefinition = "text[]")
    private String[] images = new String[0];

    @Column(nullable = false)
    private String category;

    @Column(name = "in_stock", nullable = false)
    private Boolean inStock = true;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", insertable = false, updatable = false)
    private Store store;

    @Column(name = "listing_fee", nullable = false)
    private Double listingFee = 0.0;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Column(name = "featured_until")
    private LocalDateTime featuredUntil;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "sales_count", nullable = false)
    private Integer salesCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.APPROVED;

    public enum ProductStatus {
        PENDING, APPROVED, REJECTED, DRAFT
    }
}
