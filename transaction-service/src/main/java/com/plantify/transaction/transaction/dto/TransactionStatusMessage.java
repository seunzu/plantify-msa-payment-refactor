package com.plantify.transaction.transaction.dto;

import com.plantify.transaction.transaction.domain.Status;
import com.plantify.transaction.transaction.domain.Transaction;

public record TransactionStatusMessage(
        Long transactionId,
        Long userId,
        String orderId,
        Long amount,          
        Status status
) {

    public static TransactionStatusMessage from(Transaction transaction) {
        return new TransactionStatusMessage(
                transaction.getTransactionId(),
                transaction.getUserId(),
                transaction.getOrderId(),
                transaction.getAmount(),
                transaction.getStatus()
        );
    }
}