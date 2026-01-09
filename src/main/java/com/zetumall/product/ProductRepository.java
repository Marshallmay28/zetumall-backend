package com.zetumall.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    
    List<Product> findByStoreId(String storeId);
    
    List<Product> findByStatus(Product.ProductStatus status);
    
    List<Product> findByCategory(String category);
    
    List<Product> findByStoreIdAndStatus(String storeId, Product.ProductStatus status);
    
    @Query("SELECT p FROM Product p WHERE p.isFeatured = true AND p.status = 'APPROVED' ORDER BY p.featuredUntil DESC")
    List<Product> findFeaturedProducts();
    
    @Query("SELECT p FROM Product p WHERE p.status = 'APPROVED' AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Product> searchProducts(@Param("search") String search);
    
    @Query("SELECT p FROM Product p WHERE p.status = 'APPROVED' AND " +
           "p.category = :category AND p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByCategoryAndPriceRange(
        @Param("category") String category,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice
    );
}
