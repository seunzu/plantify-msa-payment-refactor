package com.plantify.payment.payment.dto.response;

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
}