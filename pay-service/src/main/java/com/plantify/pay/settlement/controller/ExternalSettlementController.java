package com.plantify.pay.settlement.controller;

import com.plantify.pay.settlement.dto.ExternalSettlementResponse;
import com.plantify.pay.settlement.application.PaySettlementQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/pay/settlements")
@RequiredArgsConstructor
public class ExternalSettlementController {

    private final PaySettlementQueryService paySettlementQueryService;

    // 외부 서버가 orderId로 결제 정산 내역 조회
    @GetMapping("/external")
    public ResponseEntity<ExternalSettlementResponse> getSettlementByOrderId(@RequestParam String orderId) {
        ExternalSettlementResponse response = paySettlementQueryService.getSettlementByOrderId(orderId);
        return ResponseEntity.ok(response);
    }
}
