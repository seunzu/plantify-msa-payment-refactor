package com.plantify.payment.client;

import com.plantify.payment.config.FeignConfig;
import com.plantify.payment.payment.dto.request.TransactionConfirmRequest;
import com.plantify.payment.payment.dto.request.TransactionRequest;
import com.plantify.payment.payment.dto.response.TransactionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "transaction-service",
        url = "${transaction.service.url}",
        configuration = FeignConfig.class
)
public interface TransactionServiceClient {

    @GetMapping("/v1/transactions/{transactionId}")
    TransactionResponse getTransactionById(@PathVariable Long transactionId);

    @PostMapping("/v1/transactions")
    TransactionResponse createPendingTransaction(@RequestBody TransactionRequest request);

    // 결제 완료 확정 (PENDING -> COMPLETED)
    @PostMapping("/v1/transactions/confirm")
    TransactionResponse confirm(@RequestBody TransactionConfirmRequest request);

    // 환불 확정 (COMPLETED -> REFUNDED)
    @PostMapping("/v1/transactions/refunds")
    TransactionResponse refund(@RequestBody TransactionConfirmRequest request);

    // 취소 확정 (PENDING -> CANCELLED)
    @PostMapping("/v1/transactions/cancellations")
    TransactionResponse cancel(@RequestBody TransactionConfirmRequest request);
}
