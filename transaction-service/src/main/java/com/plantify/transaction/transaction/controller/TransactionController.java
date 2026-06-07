package com.plantify.transaction.transaction.controller;

import com.plantify.transaction.transaction.dto.request.TransactionConfirmRequest;
import com.plantify.transaction.transaction.dto.request.TransactionRequest;
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
        return ApiResponse.ok(transactionService.getTransactionById(transactionId));
    }

    // 트랜잭션 존재 여부 확인
    @GetMapping("/exist")
    public ApiResponse<Boolean> existTransaction(
            @RequestParam Long userId,
            @RequestParam String orderId,
            @RequestParam List<Status> statusList) {
        return ApiResponse.ok(transactionService.existTransaction(userId, orderId, statusList));
    }

    // PENDING 생성
    @PostMapping
    public ApiResponse<TransactionResponse> createPendingTransaction(@RequestBody TransactionRequest request) {
        return ApiResponse.ok(transactionService.createPendingTransaction(request));
    }

    // Payment Orchestrator
    // PENDING -> COMPLETED
    @PostMapping("/confirm")
    public ApiResponse<TransactionResponse> confirmPayment(@RequestBody TransactionConfirmRequest request) {
        return ApiResponse.ok(transactionService.confirmPayment(request));
    }

    // COMPLETED -> REFUNDED
    @PostMapping("/refunds")
    public ApiResponse<TransactionResponse> confirmRefund(@RequestBody TransactionConfirmRequest request) {
        return ApiResponse.ok(transactionService.confirmRefund(request));
    }

    // PENDING -> CANCELLED
    @PostMapping("/cancellations")
    public ApiResponse<TransactionResponse> confirmCancellation(@RequestBody TransactionConfirmRequest request) {
        return ApiResponse.ok(transactionService.confirmCancellation(request));
    }
}
