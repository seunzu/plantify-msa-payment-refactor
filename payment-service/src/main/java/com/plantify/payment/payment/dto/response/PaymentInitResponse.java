package com.plantify.payment.payment.dto.response;

public record PaymentInitResponse(
        Long paymentId,
        Long transactionId,
        String orderId,
        String orderName,
        Long amount,
        String token,
        String redirectUri
) {
}