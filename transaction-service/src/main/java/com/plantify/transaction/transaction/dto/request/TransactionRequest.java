package com.plantify.transaction.transaction.dto.request;

import com.plantify.transaction.transaction.domain.Status;
import com.plantify.transaction.transaction.domain.Transaction;

public record TransactionRequest(
        Long userId,
        Long sellerId,
        String orderId,
        String orderName,
        Long amount,
        String redirectUri
) {
    public Transaction toEntity() {
        return Transaction.builder()
                .userId(userId)
                .sellerId(sellerId)
                .orderId(orderId)
                .orderName(orderName)
                .amount(amount)
                .status(Status.PENDING)
                .redirectUri(redirectUri)
                .build();
    }
}
