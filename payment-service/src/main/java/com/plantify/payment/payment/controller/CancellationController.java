package com.plantify.payment.payment.controller;

import com.plantify.payment.payment.dto.request.CancellationRequest;
import com.plantify.payment.payment.dto.response.ProcessResponse;
import com.plantify.payment.global.response.ApiResponse;
import com.plantify.payment.payment.application.cancellation.CancellationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/payments/cancellations")
public class CancellationController {

    private final CancellationService cancellationService;

    // 모든 취소 요청 조회
    @GetMapping
    public ApiResponse<List<ProcessResponse>> getAllCancellations() {
        List<ProcessResponse> response = cancellationService.getAllCancellations();
        return ApiResponse.ok(response);
    }

    // 특정 취소 요청 조회
    @GetMapping("/{cancellationId}")
    public ApiResponse<ProcessResponse> getCancellationById(@PathVariable Long paymentId) {
        ProcessResponse response = cancellationService.getCancellationById(paymentId);
        return ApiResponse.ok(response);
    }

    // 특정 사용자 취소 요청 조회
    @GetMapping("/users/{userId}")
    public ApiResponse<List<ProcessResponse>> getCancellationsByUserId(@PathVariable Long userId) {
        List<ProcessResponse> response = cancellationService.getCancellationsByUserId(userId);
        return ApiResponse.ok(response);
    }
}
