package com.plantify.pay.client;

import com.plantify.pay.ledger.dto.process.*;
import com.plantify.pay.ledger.domain.Status;
import com.plantify.pay.global.response.ApiResponse;
import com.plantify.pay.ledger.dto.process.PayTransactionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "transaction-service", url = "${transaction.service.url}")
public interface TransactionServiceClient {

    @GetMapping("/v1/transactions/{transactionId}")
    ApiResponse<TransactionResponse> getTransactionById(@PathVariable Long transactionId);

    @GetMapping("/v1/transactions/exist")
    boolean existsByUserIdAndStatusIn(@RequestParam String orderId, @RequestParam List<Status> statusList);

    @PostMapping("/v1/transactions")
    ApiResponse<TransactionResponse> createPendingTransaction(@RequestBody TransactionRequest request);

    @PostMapping("/v1/transactions/payments")
    ApiResponse<TransactionResponse> updateTransactionToSuccess(@RequestBody PayTransactionRequest request);

    @PostMapping("/v1/transactions/refunds")
    ApiResponse<TransactionResponse> updateTransactionToRefund(@RequestBody UpdateTransactionRequest request);

    @PostMapping("/v1/transactions/cancellations")
    ApiResponse<TransactionResponse> updatePayTransactionToCancellation(@RequestBody UpdateTransactionRequest request);

}