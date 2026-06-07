package com.plantify.transaction.transaction.dto.request;

public record PaymentRequest(
        Long transactionId,
        String orderId,
        String orderName,
        Long amount
) {
}
