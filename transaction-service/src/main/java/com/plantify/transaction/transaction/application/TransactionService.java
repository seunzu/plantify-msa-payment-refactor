package com.plantify.transaction.transaction.application;

import com.plantify.transaction.transaction.dto.request.PayTransactionRequest;
import com.plantify.transaction.transaction.dto.request.TransactionRequest;
import com.plantify.transaction.transaction.dto.request.UpdateTransactionRequest;
import com.plantify.transaction.transaction.dto.response.TransactionResponse;

public interface TransactionService {

    TransactionResponse getTransactionById(Long transactionId);
    TransactionResponse createPendingTransaction(TransactionRequest request);
    TransactionResponse updateTransactionToSuccess(PayTransactionRequest request);
    TransactionResponse updateTransactionToRefund(UpdateTransactionRequest request);
    TransactionResponse updateTransactionToCancellation(UpdateTransactionRequest request);
    void failExpiredTransaction(Long transactionId);
}
