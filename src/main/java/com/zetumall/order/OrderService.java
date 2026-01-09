package com.zetumall.order;

import com.zetumall.escrow.EscrowRepository;
import com.zetumall.escrow.EscrowTransaction;
import com.zetumall.order.dto.OrderCreateRequest;
import com.zetumall.product.Product;
import com.zetumall.product.ProductRepository;
import com.zetumall.store.Store;
import com.zetumall.store.StoreRepository;
import com.zetumall.user.User;
import com.zetumall.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final EscrowRepository escrowRepository;

    /**
     * Create a new order with escrow
     */
    @Transactional
    public Order createOrder(OrderCreateRequest request, String userId) {
        // Verify store exists
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        // Verify user
        User buyer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Calculate total
        BigDecimal total = BigDecimal.ZERO;
        for (OrderCreateRequest.OrderItemRequest item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));
            
            BigDecimal itemTotal = BigDecimal.valueOf(product.getPrice())
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);
        }

        // Create order
        Order order = new Order();
        order.setBuyerId(userId);
        order.setStoreId(request.getStoreId());
        order.setTotal(total);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingPhone(request.getShippingPhone());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus("PENDING");
        order.setNotes(request.getNotes());

        Order savedOrder = orderRepository.save(order);

        // Create escrow transaction
        createEscrowForOrder(savedOrder, store);

        log.info("Order created: {} for buyer: {}", savedOrder.getId(), userId);
        
        return savedOrder;
    }

    /**
     * Create escrow transaction for order
     */
    private void createEscrowForOrder(Order order, Store store) {
        // Calculate platform fee (10% default, use store's commission rate)
        BigDecimal platformFeeRate = BigDecimal.valueOf(store.getCommissionRate() / 100);
        BigDecimal platformFee = order.getTotal().multiply(platformFeeRate);
        BigDecimal sellerAmount = order.getTotal().subtract(platformFee);

        // Generate 6-digit release code
        String releaseCode = String.format("%06d", new Random().nextInt(1000000));

        EscrowTransaction escrow = new EscrowTransaction();
        escrow.setOrderId(order.getId());
        escrow.setBuyerId(order.getBuyerId());
        escrow.setSellerId(store.getUserId());
        escrow.setAmount(order.getTotal());
        escrow.setPlatformFee(platformFee);
        escrow.setSellerAmount(sellerAmount);
        escrow.setStatus(EscrowTransaction.EscrowStatus.HELD);
        escrow.setHeldAt(LocalDateTime.now());
        escrow.setExpiresAt(LocalDateTime.now().plusDays(14));  // 14 days to deliver
        escrow.setPaymentMethod(order.getPaymentMethod());
        escrow.setReleaseCode(releaseCode);

        escrowRepository.save(escrow);
        
        log.info("Escrow created for order: {} with release code", order.getId());
    }

    /**
     * Release escrow funds using release code
     */
    @Transactional
    public void releaseEscrow(String orderId, String releaseCode, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Verify buyer owns this order
        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You don't own this order");
        }

        EscrowTransaction escrow = escrowRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Escrow not found for order"));

        // Verify release code
        if (!releaseCode.equals(escrow.getReleaseCode())) {
            throw new RuntimeException("Invalid release code");
        }

        if (escrow.getStatus() != EscrowTransaction.EscrowStatus.HELD) {
            throw new RuntimeException("Escrow is not in HELD status");
        }

        // Release funds
        escrow.setStatus(EscrowTransaction.EscrowStatus.RELEASED);
        escrow.setReleasedAt(LocalDateTime.now());
        escrowRepository.save(escrow);

        // Update order status
        order.setStatus(Order.OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("Escrow released for order: {}", orderId);
    }

    /**
     * Get order by ID
     */
    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    /**
     * Get buyer's orders
     */
    public List<Order> getBuyerOrders(String buyerId) {
        return orderRepository.findByBuyerId(buyerId);
    }

    /**
     * Get store's orders (for seller)
     */
    public List<Order> getStoreOrders(String storeId, String userId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        if (!store.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You don't own this store");
        }

        return orderRepository.findByStoreId(storeId);
    }

    /**
     * Update order status (for seller)
     */
    @Transactional
    public Order updateOrderStatus(String orderId, Order.OrderStatus status, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Store store = storeRepository.findById(order.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        if (!store.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You don't own this store");
        }

        order.setStatus(status);

        if (status == Order.OrderStatus.SHIPPED) {
            order.setShippedAt(LocalDateTime.now());
        }

        return orderRepository.save(order);
    }
}
