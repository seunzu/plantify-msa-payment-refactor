package com.plantify.payment.payment.dto.request;

public record DebitRequest(
        Long transactionId,
        Long userId,
        String orderId,
        String orderName,
        Long amount,
        Long pointToUse
) {
}