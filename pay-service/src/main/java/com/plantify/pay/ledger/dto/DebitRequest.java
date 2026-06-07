package com.plantify.pay.ledger.dto;

public record DebitRequest(
        Long transactionId,
        Long userId,
        String orderId,
        String orderName,
        Long amount,
        Long pointToUse
) {
}
