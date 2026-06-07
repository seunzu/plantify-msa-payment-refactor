package com.plantify.pay.settlement.controller;

import com.plantify.pay.ledger.dto.ExternalSettlementResponse;
import com.plantify.pay.settlement.application.PaySettlementDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/pay/settlements")
@RequiredArgsConstructor
public class PaySettlementInternalController {

    private final PaySettlementDomainService paySettlementService;

    // orderId로 결제 내역 받기
    @GetMapping("/external")
    public ResponseEntity<ExternalSettlementResponse> getSettlementByOrderId(@RequestParam String orderId) {
        ExternalSettlementResponse response = paySettlementService.getSettlementByOrderId(orderId);
        return ResponseEntity.ok(response);
    }
}