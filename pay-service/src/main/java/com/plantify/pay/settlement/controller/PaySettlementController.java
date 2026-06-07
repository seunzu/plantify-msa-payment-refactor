package com.plantify.pay.settlement.controller;

import com.plantify.pay.settlement.dto.PaySettlementResponse;
import com.plantify.pay.ledger.domain.Status;
import com.plantify.pay.global.response.ApiResponse;
import com.plantify.pay.settlement.application.PaySettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/pay/settlements")
public class PaySettlementController {

    private final PaySettlementService paySettlementService;

    // 자신의 모든 결제 내역
    @GetMapping
    public ApiResponse<Page<PaySettlementResponse>> getAllPaySettlements(Pageable pageable) {
        Page<PaySettlementResponse> response = paySettlementService.getAllPaySettlements(pageable);
        return ApiResponse.ok(response);
    }

    // 자신의 특정 결제 내역
    @GetMapping("/{status}")
    public ApiResponse<Page<PaySettlementResponse>> getPaySettlementByStatus(
            @PathVariable Status status, Pageable pageable) {
        Page<PaySettlementResponse> paySettlementByStatus = paySettlementService.getPaySettlementByStatus(status, pageable);
        return ApiResponse.ok(paySettlementByStatus);
    }

    // 한 달 동안 결제 내역
    @GetMapping("/amount")
    public ApiResponse<Long> getPaySettlementAmount() {
        Long amount = paySettlementService.getPaySettlementAmount();
        return ApiResponse.ok(amount); 
    }
}
