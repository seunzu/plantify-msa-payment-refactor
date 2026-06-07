package com.plantify.pay.ledger.application.event;

import com.plantify.pay.ledger.dto.process.TransactionStatusMessage;

public interface PayTransactionStatusService {

    void processSuccessfulTransaction(TransactionStatusMessage message);
    void processFailedTransaction(TransactionStatusMessage message);
}
