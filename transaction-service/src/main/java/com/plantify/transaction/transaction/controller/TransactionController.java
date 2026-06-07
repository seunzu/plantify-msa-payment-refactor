package com.plantify.transaction.transaction.controller;

import com.plantify.transaction.transaction.dto.request.PayTransactionRequest;
import com.plantify.transaction.transaction.dto.request.TransactionRequest;
import com.plantify.transaction.transaction.dto.request.UpdateTransactionRequest;
import com.plantify.transaction.transaction.dto.response.TransactionResponse;
import com.plantify.transaction.transaction.domain.Status;
import com.plantify.transaction.global.response.ApiResponse;
import com.plantify.transaction.transaction.application.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    // 트랜잭션 조회
    @GetMapping("/{transactionId}")
    public ApiResponse<TransactionResponse> getTransactionById(@PathVariable Long transactionId) {
        TransactionResponse response = transactionService.getTransactionById(transactionId);
        return ApiResponse.ok(response);
    }

    // 트랜잭션 존재 여부 확인
    @GetMapping("/exist")
    public boolean existTransaction(@RequestParam Long userId, @RequestParam String orderId, @RequestParam List<Status> statusList) {
        return transactionService.existTransaction(userId, orderId, statusList);
    }

    // 대기 트랜잭션 생성(PENDING)
    @PostMapping
    public ApiResponse<TransactionResponse> createPendingTransaction(@RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.createPendingTransaction(request);
        return ApiResponse.ok(response);
    }

    // 결제 트랜잭션(PNEDING -> SUCESS)
    @PostMapping("/payments")
    public ApiResponse<TransactionResponse> updateTransactionToSuccess(@RequestBody PayTransactionRequest request) {
        TransactionResponse response = transactionService.updateTransactionToSuccess(request);
        return ApiResponse.ok(response);
    }

    // 환불 트랜잭션(SUCCESS -> REFUND)
    @PostMapping("/refunds")
    public ApiResponse<TransactionResponse> updateTransactionToRefund(@RequestBody UpdateTransactionRequest request) {
        TransactionResponse response = transactionService.updateTransactionToRefund(request);
        return ApiResponse.ok(response);
    }

    // 취소 트랜잭션(PENDING -> CANCELLED)
    @PostMapping("/cancellations")
    public ApiResponse<TransactionResponse> updatePayTransactionToCancellation(@RequestBody UpdateTransactionRequest request) {
        TransactionResponse response = transactionService.updateTransactionToCancellation(request);
        return ApiResponse.ok(response);
    }
}
