package com.plantify.payment.payment.application.refund;

import com.plantify.payment.payment.dto.request.RefundRequest;
import com.plantify.payment.payment.dto.response.PaymentResponse;

public interface RefundOrchestrator {

    PaymentResponse processRefund(RefundRequest request);
}
