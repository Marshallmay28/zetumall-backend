package com.zetumall.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {
    private String name;
    private String description;
    private Double mrp;
    private Double price;
    private String[] images;
    private String category;
    private Boolean inStock = true;
    private String storeId;
}
