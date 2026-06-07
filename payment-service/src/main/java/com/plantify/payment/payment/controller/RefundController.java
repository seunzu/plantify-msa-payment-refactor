package com.plantify.payment.payment.controller;

import com.plantify.payment.payment.dto.response.ProcessResponse;
import com.plantify.payment.global.response.ApiResponse;
import com.plantify.payment.payment.application.refund.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/payments/refunds")
public class RefundController {

    private final RefundService refundService;

    // 모든 환불 조회
    @GetMapping
    public ApiResponse<List<ProcessResponse>> getAllRefunds() {
        List<ProcessResponse> response = refundService.getAllRefunds();
        return ApiResponse.ok(response);
    }

    // 특정 환불 조회
    @GetMapping("/{refundId}")
    public ApiResponse<ProcessResponse> getRefundById(@PathVariable Long paymentId) {
        ProcessResponse response = refundService.getRefundById(paymentId);
        return ApiResponse.ok(response);
    }

    // 특정 사용자 환불 조회
    @GetMapping("/users/{userId}")
    public ApiResponse<List<ProcessResponse>> getRefundsByUserId(@PathVariable Long userId) {
        List<ProcessResponse> response = refundService.getRefundsByUserId(userId);
        return ApiResponse.ok(response);
    }
}