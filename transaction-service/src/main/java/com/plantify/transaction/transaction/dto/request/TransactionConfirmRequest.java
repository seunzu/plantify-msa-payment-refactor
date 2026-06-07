package com.plantify.transaction.transaction.dto.request;

public record TransactionConfirmRequest(
        Long transactionId,
        Long paymentId
) {
}
