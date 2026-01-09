package com.zetumall.payment.dto;

import lombok.Data;

@Data
public class MpesaCallback {
    // This structure maps the JSON payload from M-Pesa callback
    // Simplified for now, actual structure is nested
    private Body Body;

    @Data
    public static class Body {
        private stkCallback stkCallback;
    }

    @Data
    public static class stkCallback {
        private String MerchantRequestID;
        private String CheckoutRequestID;
        private Integer ResultCode;
        private String ResultDesc;
        private CallbackMetadata CallbackMetadata;
    }

    @Data
    public static class CallbackMetadata {
        private Item[] Item;
    }

    @Data
    public static class Item {
        private String Name;
        private Object Value;
    }
}
