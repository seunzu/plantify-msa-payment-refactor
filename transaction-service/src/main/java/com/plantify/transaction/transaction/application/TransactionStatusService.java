package com.plantify.transaction.transaction.application;

import com.plantify.transaction.transaction.dto.TransactionStatusMessage;

public interface TransactionStatusService {

    void processSuccessfulTransaction(TransactionStatusMessage message);
    void processFailedTransaction(TransactionStatusMessage message);
}
