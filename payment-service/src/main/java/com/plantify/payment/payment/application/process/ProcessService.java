package com.plantify.payment.payment.application.process;

import com.plantify.payment.payment.dto.request.CancellationRequest;
import com.plantify.payment.payment.dto.request.RefundRequest;
import com.plantify.payment.payment.dto.response.ProcessResponse;
import com.plantify.payment.payment.dto.request.PaymentRequest;

public interface ProcessService {

    ProcessResponse processPayment(PaymentRequest request);
    ProcessResponse processRefund(RefundRequest request);
    ProcessResponse processCancellation(CancellationRequest request);
}
