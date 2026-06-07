package com.plantify.transaction.transaction.dto.response;

import com.plantify.transaction.transaction.domain.Transaction;

import java.time.LocalDateTime;

public record TransactionResponse(
        Long transactionId,
        Long userId,
        Long paymentId,
        String orderId,
        String orderName,
        Long amount,
        Long sellerId,
        String status,
        String reason,
        String redirectUri,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getTransactionId(),
                transaction.getUserId(),
                transaction.getPaymentId(),
                transaction.getOrderId(),
                transaction.getOrderName(),
                transaction.getAmount(),
                transaction.getSellerId(),
                transaction.getStatus().name(),
                transaction.getReason(),
                transaction.getRedirectUri(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}
