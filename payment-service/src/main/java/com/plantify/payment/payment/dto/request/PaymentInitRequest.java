package com.plantify.payment.payment.dto.request;

public record PaymentInitRequest(
        Long sellerId,
        String orderId,
        String orderName,
        Long amount,
        String redirectUri
) {
}
