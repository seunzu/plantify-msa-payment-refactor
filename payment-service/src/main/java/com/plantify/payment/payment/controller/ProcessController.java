package com.plantify.payment.payment.controller;

import com.plantify.payment.payment.dto.request.CancellationRequest;
import com.plantify.payment.payment.dto.request.PaymentRequest;
import com.plantify.payment.payment.dto.request.RefundRequest;
import com.plantify.payment.payment.dto.response.ProcessResponse;
import com.plantify.payment.global.response.ApiResponse;
import com.plantify.payment.payment.application.process.ProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/payments/process")
public class ProcessController {

    private final ProcessService processService;

    // 결제
    @PostMapping
    public ApiResponse<ProcessResponse> processPayment(@RequestBody PaymentRequest request) {
        ProcessResponse response = processService.processPayment(request);
        return ApiResponse.ok(response);
    }

    // 환불
    @PostMapping("/refunds")
    public ApiResponse<ProcessResponse> processRefund(@RequestBody RefundRequest request) {
        ProcessResponse response = processService.processRefund(request);
        return ApiResponse.ok(response);
    }

    // 취소
    @PostMapping("/cancellations")
    public ApiResponse<ProcessResponse> processCancellation(@RequestBody CancellationRequest request) {
        ProcessResponse response = processService.processCancellation(request);
        return ApiResponse.ok(response);
    }
}
