package com.plantify.transaction.transaction.application;

import com.plantify.transaction.transaction.dto.request.TransactionConfirmRequest;
import com.plantify.transaction.transaction.dto.request.TransactionRequest;
import com.plantify.transaction.transaction.dto.response.TransactionResponse;

public interface TransactionService {

    TransactionResponse getTransactionById(Long transactionId);

    TransactionResponse createPendingTransaction(TransactionRequest request);
    TransactionResponse confirmPayment(TransactionConfirmRequest request);
    TransactionResponse confirmRefund(TransactionConfirmRequest request);
    TransactionResponse confirmCancellation(TransactionConfirmRequest request);
    TransactionResponse confirmFailure(TransactionConfirmRequest request);
}
