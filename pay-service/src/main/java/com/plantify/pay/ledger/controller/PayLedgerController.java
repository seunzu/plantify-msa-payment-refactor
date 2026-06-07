package com.plantify.pay.ledger.controller;

import com.plantify.pay.ledger.dto.CreditRequest;
import com.plantify.pay.ledger.dto.DebitRequest;
import com.plantify.pay.global.response.ApiResponse;
import com.plantify.pay.ledger.application.LedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/pay")
public class PayLedgerController {

    private final LedgerService ledgerService;

    // Payment Orchestrator
    // 결제 시 잔액 차감
    @PostMapping("/debit")
    public ApiResponse<Void> debit(@RequestBody DebitRequest request) {
        ledgerService.debit(request);
        return ApiResponse.ok();
    }

    // 환불 시 잔액 복원
    @PostMapping("/credit")
    public ApiResponse<Void> credit(@RequestBody CreditRequest request) {
        ledgerService.credit(request);
        return ApiResponse.ok();
    }
}
