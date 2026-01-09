package com.zetumall.store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, String> {
    
    Optional<Store> findByUserId(String userId);
    
    Optional<Store> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    List<Store> findByStatus(Store.StoreStatus status);
    
    List<Store> findByUserIdAndStatus(String userId, Store.StoreStatus status);
}
