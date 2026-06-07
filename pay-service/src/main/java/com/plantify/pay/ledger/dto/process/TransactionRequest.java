package com.plantify.pay.ledger.dto.process;

import java.util.UUID;

public record TransactionRequest(
        Long sellerId,
        String orderId,
        String orderName,
        Long amount,
        String redirectUri
) {

    public static TransactionRequest from(PendingTransactionRequest request) {
        return new TransactionRequest(
                request.sellerId(),
                UUID.randomUUID().toString(),
                request.orderName(),
                request.amount(),
                request.redirectUri()
        );
    }
}
