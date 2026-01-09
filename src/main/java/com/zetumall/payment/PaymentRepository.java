package com.zetumall.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentTransaction, String> {
    
    Optional<PaymentTransaction> findByCheckoutRequestId(String checkoutRequestId);
    
    List<PaymentTransaction> findByOrderId(String orderId);
    
    List<PaymentTransaction> findByUserId(String userId);
}
