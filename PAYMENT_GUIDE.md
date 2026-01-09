# Payment Integration Guide

## Overview
ZetuMall uses M-Pesa for payment processing. The integration supports:
1. **STK Push (Lipa na M-Pesa Online)**: Initiates a payment request to the user's phone.
2. **Callbacks**: Handles asynchronous payment confirmation from Safaricom.
3. **Status Checks**: Allows checking the status of a transaction.

## Configuration
Update your `.env` or `application.yml` with:
```properties
MPESA_CONSUMER_KEY=your_key
MPESA_CONSUMER_SECRET=your_secret
MPESA_PASSKEY=your_passkey
MPESA_SHORTCODE=174379
MPESA_CALLBACK_URL=https://your-domain.com/api/payments/mpesa/callback
```

## API Endpoints

### 1. Initiate Payment
**POST** `/api/payments/mpesa/stk-push`
```json
{
  "orderId": "order_123",
  "phoneNumber": "2547XXXXXXXX",
  "amount": 1000.0
}
```

### 2. Check Status
**GET** `/api/payments/{orderId}/status`

### 3. Webhook (Internal)
**POST** `/api/payments/mpesa/callback`
(This endpoint is called by Safaricom, not the frontend)

## Flow
1. User clicks "Pay with M-Pesa" on frontend.
2. Frontend calls `/api/payments/mpesa/stk-push`.
3. Backend calls Safaricom API.
4. User enters PIN on phone.
5. Safaricom sends callback to `/api/payments/mpesa/callback`.
6. Backend updates order status to `PAID`.
