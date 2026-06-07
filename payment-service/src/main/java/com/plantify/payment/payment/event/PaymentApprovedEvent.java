package com.plantify.payment.payment.event;

import com.plantify.payment.payment.domain.entity.Payment;

public record PaymentApprovedEvent(
        Long paymentId,
        Long transactionId,
        Long userId,
        String orderId,
        String orderName,
        Long amount,
        Long pointUsed
) {
    public static PaymentApprovedEvent from(Payment payment) {
        return new PaymentApprovedEvent(
                payment.getPaymentId(),
                payment.getTransactionId(),
                payment.getUserId(),
                payment.getOrderId(),
                payment.getOrderName(),
                payment.getAmount(),
                payment.getPointUsed()
        );
    }
}
