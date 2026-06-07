package com.plantify.payment.payment.application.payment;

import com.plantify.payment.payment.dto.request.PaymentInitRequest;
import com.plantify.payment.payment.dto.response.PaymentInitResponse;
import com.plantify.payment.payment.dto.response.PaymentResponse;

public interface PaymentOrchestrator {

    PaymentInitResponse initiate(PaymentInitRequest request);
    PaymentResponse processPayment(String token, Long pointToUse);
}
