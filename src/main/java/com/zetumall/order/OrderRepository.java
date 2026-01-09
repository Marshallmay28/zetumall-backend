package com.zetumall.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    
    List<Order> findByBuyerId(String buyerId);
    
    List<Order> findByStoreId(String storeId);
    
    List<Order> findByBuyerIdAndStatus(String buyerId, Order.OrderStatus status);
    
    List<Order> findByStoreIdAndStatus(String storeId, Order.OrderStatus status);
}
