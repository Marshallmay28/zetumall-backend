package com.zetumall.payment;

import com.zetumall.order.Order;
import com.zetumall.order.OrderRepository;
import com.zetumall.payment.dto.MpesaCallback;
import com.zetumall.payment.dto.PaymentRequest;
import com.zetumall.user.User;
import com.zetumall.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import jakarta.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final WebClient.Builder webClientBuilder;
    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = webClientBuilder.build();
    }

    @Value("${mpesa.consumer-key}")
    private String consumerKey;

    @Value("${mpesa.consumer-secret}")
    private String consumerSecret;

    @Value("${mpesa.passkey}")
    private String passkey;

    @Value("${mpesa.shortcode}")
    private String shortcode;

    @Value("${mpesa.callback-url}")
    private String callbackUrl;

    @Value("${mpesa.auth-url}")
    private String authUrl;

    @Value("${mpesa.stk-push-url}")
    private String stkPushUrl;

    /**
     * Initiate STK Push payment
     */
    @Transactional
    public PaymentTransaction initiatePayment(PaymentRequest request, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Create transaction record
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrderId(order.getId());
        transaction.setUserId(userId);
        transaction.setAmount(request.getAmount());
        transaction.setPhoneNumber(request.getPhoneNumber());
        transaction.setStatus(PaymentTransaction.PaymentStatus.PENDING);

        // In a real scenario, we would generate this, but M-Pesa returns it
        // We'll save it first to get an ID if needed, or update later
        PaymentTransaction savedTx = paymentRepository.save(transaction);

        try {
            // Get Access Token
            String accessToken = getAccessToken();

            // Prepare Password
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String password = Base64.getEncoder().encodeToString(
                    (shortcode + passkey + timestamp).getBytes(StandardCharsets.UTF_8));

            // Prepare Request Body
            Map<String, Object> body = new HashMap<>();
            body.put("BusinessShortCode", shortcode);
            body.put("Password", password);
            body.put("Timestamp", timestamp);
            body.put("TransactionType", "CustomerPayBillOnline");
            body.put("Amount", request.getAmount().intValue()); // M-Pesa often takes integers
            body.put("PartyA", request.getPhoneNumber());
            body.put("PartyB", shortcode);
            body.put("PhoneNumber", request.getPhoneNumber());
            body.put("CallBackURL", callbackUrl);
            body.put("AccountReference", order.getId().substring(0, Math.min(order.getId().length(), 12)));
            body.put("TransactionDesc", "Payment for order");

            // Make STK Push Request
            Map response = webClient.post()
                    .uri(stkPushUrl)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null) {
                String merchantRequestId = (String) response.get("MerchantRequestID");
                String checkoutRequestId = (String) response.get("CheckoutRequestID");
                String responseCode = (String) response.get("ResponseCode");

                savedTx.setMerchantRequestId(merchantRequestId);
                savedTx.setCheckoutRequestId(checkoutRequestId);
                savedTx.setFailureReason("ResponseCode: " + responseCode); // Temp storage

                log.info("STK Push initiated: MerchantID={}, CheckoutID={}", merchantRequestId, checkoutRequestId);
            }

        } catch (Exception e) {
            log.error("Error initiating M-Pesa payment", e);
            savedTx.setStatus(PaymentTransaction.PaymentStatus.FAILED);
            savedTx.setFailureReason(e.getMessage());
        }

        return paymentRepository.save(savedTx);
    }

    /**
     * Handle M-Pesa Callback
     */
    @Transactional
    public void processCallback(MpesaCallback callback) {
        log.info("Processing M-Pesa callback: {}", callback);

        MpesaCallback.stkCallback stkCallback = callback.getBody().getStkCallback();
        String checkoutRequestId = stkCallback.getCheckoutRequestID();
        Integer resultCode = stkCallback.getResultCode();

        PaymentTransaction transaction = paymentRepository.findByCheckoutRequestId(checkoutRequestId)
                .orElse(null);

        if (transaction == null) {
            log.error("Transaction not found for CheckoutRequestID: {}", checkoutRequestId);
            return;
        }

        if (resultCode == 0) {
            // Success
            transaction.setStatus(PaymentTransaction.PaymentStatus.COMPLETED);

            // Extract details metadata
            if (stkCallback.getCallbackMetadata() != null && stkCallback.getCallbackMetadata().getItem() != null) {
                for (MpesaCallback.Item item : stkCallback.getCallbackMetadata().getItem()) {
                    if ("MpesaReceiptNumber".equals(item.getName())) {
                        transaction.setReceiptNumber((String) item.getValue());
                    }
                    if ("TransactionDate".equals(item.getName())) {
                        transaction.setTransactionDate(String.valueOf(item.getValue()));
                    }
                }
            }

            // Update Order Status to PAID/CONFIRMED if needed logic here anywhere
            Order order = orderRepository.findById(transaction.getOrderId()).orElse(null);
            if (order != null) {
                order.setPaymentStatus("PAID");
                orderRepository.save(order);
            }

        } else {
            // Failure
            transaction.setStatus(PaymentTransaction.PaymentStatus.FAILED);
            transaction.setFailureReason(stkCallback.getResultDesc());
        }

        paymentRepository.save(transaction);
    }

    /**
     * Get M-Pesa Access Token
     */
    private String getAccessToken() {
        String authString = consumerKey + ":" + consumerSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));

        Map response = webClient.get()
                .uri(authUrl)
                .header("Authorization", "Basic " + encodedAuth)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response != null && response.containsKey("access_token")) {
            return (String) response.get("access_token");
        }

        throw new RuntimeException("Failed to retrieve M-Pesa access token");
    }

    /**
     * Check transaction status
     */
    public PaymentTransaction getStatus(String orderId) {
        return paymentRepository.findByOrderId(orderId).stream()
                .reduce((first, second) -> second) // Get latest
                .orElseThrow(() -> new RuntimeException("No payment found for this order"));
    }
}
