package com.plantify.pay.ledger.dto.process;

import com.plantify.pay.ledger.domain.TransactionStatus;

public record TransactionStatusMessage(
        Long transactionId,
        Long userId,
        String orderId,
        Long amount,
        TransactionStatus status
){}