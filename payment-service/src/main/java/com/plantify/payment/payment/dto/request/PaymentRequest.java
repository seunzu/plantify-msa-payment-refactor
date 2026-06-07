package com.plantify.payment.payment.dto.request;

import com.plantify.payment.payment.domain.entity.Method;
import com.plantify.payment.payment.domain.entity.Payment;
import com.plantify.payment.payment.domain.entity.Status;

public record PaymentRequest(
        Long transactionId,
        String orderId,
        String orderName,
        Long amount
) {

    public Payment toEntity(Long userId) {
        return Payment.builder()
                .userId(userId)
                .transactionId(transactionId)
                .orderId(orderId)
                .orderName(orderName)
                .amount(amount)
                .method(Method.PAY)
                .status(Status.PENDING)
                .build();
    }
}