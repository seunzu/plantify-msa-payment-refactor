package com.plantify.transaction.transaction.dto.response;

import com.plantify.transaction.transaction.domain.Status;

import java.time.LocalDateTime;

public record ProcessResponse(
        Long paymentId,
        Long userId,
        Long transactionId,
        String orderId,
        String orderName,
        Long amount,
        Status status,
        String method,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}