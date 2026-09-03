package com.plantify.pay.ledger.controller;

import com.plantify.pay.ledger.dto.process.*;
import com.plantify.pay.global.response.ApiResponse;
import com.plantify.pay.ledger.application.facade.PayFacadeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/pay")
public class PayProcessController {

    private final PayFacadeService payFacadeService;

    // 페이 결제 요청(Pending)
    @PostMapping("/payment")
    public String initiatePayment(@RequestBody PendingTransactionRequest request)  {
        PaymentResponse paymentResponse = payFacadeService.createPayTransaction(request);
        return paymentResponse.token();
    }

    // 결제 페이지 진입 정보 조회
    @GetMapping("/payment/checkout")
    public ApiResponse<TransactionStatusResponse> getPaymentCheckout(
            @RequestHeader String token) {
        TransactionStatusResponse status = payFacadeService.getPaymentCheckout(token);
        return ApiResponse.ok(status);
    }

    // 토큰 검증 및 결제 요청
    @GetMapping("/payment")
    public ApiResponse<ProcessPaymentResponse> verifyAndProcessPayment(@RequestHeader String token, @RequestParam Long pointToUse) {
        ProcessPaymentResponse status = payFacadeService.verifyAndProcessPayment(token, pointToUse);
        return ApiResponse.ok(status);
    }

    // 결제 환불 요청
    @PostMapping("/refund")
    public ApiResponse<ProcessPaymentResponse> refund(@RequestBody UpdateTransactionRequest request) {
        ProcessPaymentResponse response = payFacadeService.refund(request);
        return ApiResponse.ok(response);
    }

    // 결제 취소 요청
    @PostMapping("/cancellation")
    public ApiResponse<ProcessPaymentResponse> cancellation(@RequestBody UpdateTransactionRequest request) {
        ProcessPaymentResponse response = payFacadeService.cancellation(request);
        return ApiResponse.ok(response);
    }

}
