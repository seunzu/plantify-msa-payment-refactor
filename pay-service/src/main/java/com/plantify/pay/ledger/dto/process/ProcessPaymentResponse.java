package com.plantify.pay.ledger.dto.process;

import com.plantify.pay.ledger.domain.Status;

import java.time.LocalDateTime;

public record ProcessPaymentResponse(
        Long transactionId,
        Long userId,
        Long paymentId,
        String orderId,
        String orderName,
        Long sellerId,
        Long amount,
        Long pointToUse,
        Status status,
        String redirectUri,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ProcessPaymentResponse from(TransactionResponse tx) {
        return new ProcessPaymentResponse(
                tx.transactionId(),
                tx.userId(),
                tx.paymentId(),
                tx.orderId(),
                tx.orderName(),
                tx.sellerId(),
                tx.amount(),
                0L,
                tx.status(),
                tx.redirectUri(),
                tx.createdAt(),
                tx.updatedAt()
        );
    }
}
