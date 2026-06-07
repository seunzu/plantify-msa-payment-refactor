package com.plantify.pay.ledger.dto.process;

public record UpdateTransactionRequest(
        String orderId,
        String reason
) {
}
