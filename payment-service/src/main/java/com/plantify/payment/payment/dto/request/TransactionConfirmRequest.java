package com.plantify.payment.payment.dto.request;

public record TransactionConfirmRequest(
        Long transactionId,
        Long paymentId
) {
}