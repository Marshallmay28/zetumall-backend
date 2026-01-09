package com.zetumall.escrow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EscrowRepository extends JpaRepository<EscrowTransaction, String> {
    
    Optional<EscrowTransaction> findByOrderId(String orderId);
    
    List<EscrowTransaction> findByBuyerId(String buyerId);
    
    List<EscrowTransaction> findBySellerId(String sellerId);
    
    List<EscrowTransaction> findByStatus(EscrowTransaction.EscrowStatus status);
    
    List<EscrowTransaction> findBySellerIdAndStatus(String sellerId, EscrowTransaction.EscrowStatus status);
}
