package com.plantify.transaction.transaction.dto.request;

public record UpdateTransactionRequest(
        String orderId,
        String reason
) {
}
