package com.plantify.transaction.client;

import com.plantify.transaction.transaction.dto.request.CancellationRequest;
import com.plantify.transaction.transaction.dto.request.PaymentRequest;
import com.plantify.transaction.transaction.dto.request.RefundRequest;
import com.plantify.transaction.transaction.dto.response.ProcessResponse;
import com.plantify.transaction.global.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", url = "${payment.service.url}")
public interface PaymentServiceClient {

    @PostMapping("/v1/payments/process")
    ApiResponse<ProcessResponse> processPayment(@RequestBody PaymentRequest request);

    @PostMapping("/v1/payments/process/refunds")
    ApiResponse<ProcessResponse> processRefund(@RequestBody RefundRequest request);

    @PostMapping("/v1/payments/process/cancellations")
    ApiResponse<ProcessResponse> processCancellation(@RequestBody CancellationRequest request);

}