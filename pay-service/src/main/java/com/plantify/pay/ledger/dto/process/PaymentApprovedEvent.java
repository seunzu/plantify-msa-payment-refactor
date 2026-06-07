package com.plantify.pay.ledger.dto.process;

public record PaymentApprovedEvent(
        Long paymentId,
        Long transactionId,
        Long userId,
        String orderId,
        String orderName,
        Long amount,
        Long pointUsed
) {
}
