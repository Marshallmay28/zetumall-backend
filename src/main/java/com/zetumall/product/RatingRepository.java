package com.zetumall.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, String> {
    
    List<Rating> findByProductId(String productId);
    
    List<Rating> findByUserId(String userId);
    
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.productId = :productId")
    Double getAverageRatingForProduct(String productId);
    
    long countByProductId(String productId);
}
