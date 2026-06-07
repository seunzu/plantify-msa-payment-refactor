package com.plantify.payment.payment.dto.response;

import com.plantify.payment.payment.domain.entity.Payment;
import com.plantify.payment.payment.domain.entity.Status;

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
    public static ProcessResponse from(Payment payment) {
        return new ProcessResponse(
                payment.getPaymentId(),
                payment.getUserId(),
                payment.getTransactionId(),
                payment.getOrderId(),
                payment.getOrderName(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getMethod().name(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}