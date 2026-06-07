package com.plantify.payment.payment.controller;

import com.plantify.payment.payment.dto.request.CancellationRequest;
import com.plantify.payment.payment.dto.request.PaymentInitRequest;
import com.plantify.payment.payment.dto.request.RefundRequest;
import com.plantify.payment.payment.dto.response.PaymentInitResponse;
import com.plantify.payment.payment.dto.response.PaymentResponse;
import com.plantify.payment.global.response.ApiResponse;
import com.plantify.payment.payment.application.cancellation.CancellationOrchestrator;
import com.plantify.payment.payment.application.payment.PaymentOrchestrator;
import com.plantify.payment.payment.application.refund.RefundOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/payments")
public class PaymentController {

    private final PaymentOrchestrator paymentOrchestrator;
    private final RefundOrchestrator refundOrchestrator;
    private final CancellationOrchestrator cancellationOrchestrator;

    // 결제 실행
    @PostMapping("/initiate")
    public ApiResponse<PaymentInitResponse> initiatePayment(@RequestBody PaymentInitRequest request) {
        return ApiResponse.ok(paymentOrchestrator.initiate(request));
    }

    @PostMapping
    public ApiResponse<PaymentResponse> processPayment(
            @RequestHeader String token,
            @RequestParam(required = false, defaultValue = "0") Long pointToUse) {
        return ApiResponse.ok(paymentOrchestrator.processPayment(token, pointToUse));
    }

    // 환불 실행
    @PostMapping("/refunds")
    public ApiResponse<PaymentResponse> processRefund(@RequestBody RefundRequest request) {
        return ApiResponse.ok(refundOrchestrator.processRefund(request));
    }

    // 취소 실행
    @PostMapping("/cancellations")
    public ApiResponse<PaymentResponse> processCancellation(@RequestBody CancellationRequest request) {
        return ApiResponse.ok(cancellationOrchestrator.processCancellation(request));
    }
}