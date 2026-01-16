package com.zetumall.escrow;

import com.zetumall.admin.audit.AdminAuditRepository;
import com.zetumall.order.Order;
import com.zetumall.order.OrderRepository;
import com.zetumall.user.User;
import com.zetumall.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EscrowService {

    private final EscrowRepository escrowRepository;
    private final OrderRepository orderRepository;

    @Value("${escrow.api-key}")
    private String escrowApiKey;

    /**
     * Initiate Escrow for an Order
     * Usually called internally when an order is paid.
     */
    @Transactional
    public EscrowTransaction initiateEscrow(String orderId, String paymentMethod, String paymentRef) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (escrowRepository.findByOrderId(orderId).isPresent()) {
            throw new RuntimeException("Escrow already exists for this order");
        }

        BigDecimal amount = order.getTotal();
        BigDecimal platformFee = amount.multiply(BigDecimal.valueOf(0.05)); // 5% fee example
        BigDecimal sellerAmount = amount.subtract(platformFee);

        EscrowTransaction escrow = new EscrowTransaction();
        escrow.setOrder(order);
        escrow.setOrderId(orderId);
        escrow.setBuyer(order.getBuyer());
        escrow.setBuyerId(order.getBuyerId());

        // Order is linked directly to a store
        if (order.getStore() == null) {
            throw new RuntimeException("Order has no store associated");
        }

        String sellerId = order.getStore().getUserId(); // Store uses userId for owner
        escrow.setSellerId(sellerId);

        escrow.setAmount(amount);
        escrow.setPlatformFee(platformFee);
        escrow.setSellerAmount(sellerAmount);
        escrow.setStatus(EscrowTransaction.EscrowStatus.HELD);
        escrow.setHeldAt(LocalDateTime.now());
        // Auto-expire/release in 14 days if no action
        escrow.setExpiresAt(LocalDateTime.now().plusDays(14));
        escrow.setPaymentMethod(paymentMethod);
        escrow.setPaymentRef(paymentRef);
        escrow.setReleaseCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        log.info("Escrow initiated for Order: {}", orderId);
        return escrowRepository.save(escrow);
    }

    /**
     * Release Funds to Seller
     * Called by Finance Admin or System upon delivery confirmation + cooling
     * period.
     */
    @Transactional
    public EscrowTransaction releaseEscrow(String escrowId, String adminId) {
        EscrowTransaction escrow = escrowRepository.findById(escrowId)
                .orElseThrow(() -> new RuntimeException("Escrow transaction not found"));

        if (escrow.getStatus() != EscrowTransaction.EscrowStatus.HELD
                && escrow.getStatus() != EscrowTransaction.EscrowStatus.DISPUTED) {
            throw new RuntimeException("Escrow status must be HELD or DISPUTED to release");
        }

        // Logic to actually transfer funds (mocked)
        log.info("TRANSFERRING {} to Seller {} [MOCK BANK TRANSFER]", escrow.getSellerAmount(), escrow.getSellerId());

        escrow.setStatus(EscrowTransaction.EscrowStatus.RELEASED);
        escrow.setReleasedAt(LocalDateTime.now());
        escrow.setNotes("Released by Admin: " + adminId);

        return escrowRepository.save(escrow);
    }

    /**
     * Refund Funds to Buyer
     */
    @Transactional
    public EscrowTransaction refundEscrow(String escrowId, String reason, String adminId) {
        EscrowTransaction escrow = escrowRepository.findById(escrowId)
                .orElseThrow(() -> new RuntimeException("Escrow transaction not found"));

        if (escrow.getStatus() == EscrowTransaction.EscrowStatus.RELEASED) {
            throw new RuntimeException("Funds already released, cannot refund from Escrow");
        }

        // Logic to actually refund funds (mocked)
        log.info("REFUNDING {} to Buyer {} [MOCK REFUND]", escrow.getAmount(), escrow.getBuyerId());

        escrow.setStatus(EscrowTransaction.EscrowStatus.REFUNDED);
        escrow.setRefundedAt(LocalDateTime.now());
        escrow.setNotes("Refunded by Admin: " + adminId + ". Reason: " + reason);

        return escrowRepository.save(escrow);
    }

    /**
     * Dispute Transaction
     */
    @Transactional
    public EscrowTransaction disputeEscrow(String escrowId, String reason, String reporterId) {
        EscrowTransaction escrow = escrowRepository.findById(escrowId)
                .orElseThrow(() -> new RuntimeException("Escrow transaction not found"));

        if (escrow.getStatus() != EscrowTransaction.EscrowStatus.HELD) {
            throw new RuntimeException("Can only dispute HELD transactions");
        }

        escrow.setStatus(EscrowTransaction.EscrowStatus.DISPUTED);
        escrow.setNotes("Dispute raised by: " + reporterId + ". Reason: " + reason);

        return escrowRepository.save(escrow);
    }

    public List<EscrowTransaction> getAllTransactions() {
        return escrowRepository.findAll();
    }

    public EscrowTransaction getTransaction(String id) {
        return escrowRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
    }
}
