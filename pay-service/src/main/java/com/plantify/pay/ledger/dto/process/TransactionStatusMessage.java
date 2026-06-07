package com.plantify.pay.ledger.dto.process;

import com.plantify.pay.ledger.domain.Status;

public record TransactionStatusMessage(
        Long transactionId,
        Long userId,
        String orderId,
        Long amount,
        Status status
){}