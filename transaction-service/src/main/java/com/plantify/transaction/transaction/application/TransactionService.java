package com.plantify.transaction.transaction.application;

import com.plantify.transaction.transaction.dto.request.TransactionConfirmRequest;
import com.plantify.transaction.transaction.dto.request.TransactionRequest;
import com.plantify.transaction.transaction.dto.response.TransactionResponse;
import com.plantify.transaction.transaction.domain.Status;

import java.util.List;

public interface TransactionService {

    TransactionResponse getTransactionById(Long transactionId);
    boolean existTransaction(Long userId, String orderId, List<Status> statuses);

    TransactionResponse createPendingTransaction(TransactionRequest request);
    TransactionResponse confirmPayment(TransactionConfirmRequest request);
    TransactionResponse confirmRefund(TransactionConfirmRequest request);
    TransactionResponse confirmCancellation(TransactionConfirmRequest request);
    TransactionResponse confirmFailure(TransactionConfirmRequest request);
}
