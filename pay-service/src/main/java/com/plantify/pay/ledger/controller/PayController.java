package com.plantify.pay.ledger.controller;

import com.plantify.pay.account.dto.AccountRequest;
import com.plantify.pay.ledger.dto.PayRequest;
import com.plantify.pay.ledger.dto.PayResponse;
import com.plantify.pay.global.response.ApiResponse;
import com.plantify.pay.ledger.application.PayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/pay")
public class PayController {

    private final PayService payService;

    // 자신의 페이 조회(사용자 당 1개)
    @GetMapping
    public ApiResponse<PayResponse> getPay() {
        PayResponse response = payService.getPay();
        return ApiResponse.ok(response);
    }

    // 페이 생성(사용자 당 1개)
    @PostMapping
    public ApiResponse<PayResponse> createPay(@RequestBody AccountRequest request) {
        PayResponse response = payService.createPay(request);
        return ApiResponse.ok(response);
    }

    // 페이 수정(잔액 충전)
    @PutMapping("/recharge")
    public ApiResponse<PayResponse> balanceRechargePay(@RequestBody PayRequest request) {
        PayResponse response = payService.balanceRechargePay(request);
        return ApiResponse.ok(response);
    }
}