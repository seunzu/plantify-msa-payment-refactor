package com.plantify.payment.payment.dto.response;

import com.plantify.payment.payment.domain.entity.Payment;
import com.plantify.payment.payment.domain.entity.Status;

import java.time.LocalDateTime;

public record PaymentResponse(
        Long paymentId,
        Long userId,
        Long transactionId,
        String orderId,
        String orderName,
        Long amount,
        Long pointUsed,
        Status status,
        String method,
        String reason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getPaymentId(),
                payment.getUserId(),
                payment.getTransactionId(),
                payment.getOrderId(),
                payment.getOrderName(),
                payment.getAmount(),
                payment.getPointUsed(),
                payment.getStatus(),
                payment.getMethod().name(),
                payment.getReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}