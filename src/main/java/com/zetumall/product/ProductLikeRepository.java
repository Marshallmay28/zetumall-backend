package com.zetumall.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductLikeRepository extends JpaRepository<ProductLike, String> {
    
    Optional<ProductLike> findByUserIdAndProductId(String userId, String productId);
    
    List<ProductLike> findByUserId(String userId);
    
    List<ProductLike> findByProductId(String productId);
    
    boolean existsByUserIdAndProductId(String userId, String productId);
    
    void deleteByUserIdAndProductId(String userId, String productId);
}
