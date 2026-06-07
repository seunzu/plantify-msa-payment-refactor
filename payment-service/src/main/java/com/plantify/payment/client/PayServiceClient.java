package com.plantify.payment.client;

import com.plantify.payment.config.FeignConfig;
import com.plantify.payment.payment.dto.request.CreditRequest;
import com.plantify.payment.payment.dto.request.DebitRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "pay-service",
        url = "${pay.service.url}",
        configuration = FeignConfig.class
)
public interface PayServiceClient {

    // 결제 시 잔액 차감 (포인트 포함)
    @PostMapping("/v1/pay/debit")
    Void debit(@RequestBody DebitRequest request);

    // 환불 시 잔액 복원 (포인트 포함)
    @PostMapping("/v1/pay/credit")
    Void credit(@RequestBody CreditRequest request);
}
