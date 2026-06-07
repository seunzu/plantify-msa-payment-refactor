package com.plantify.payment.payment.application.cancellation;

import com.plantify.payment.payment.dto.request.CancellationRequest;
import com.plantify.payment.payment.dto.response.PaymentResponse;

public interface CancellationOrchestrator {

    PaymentResponse processCancellation(CancellationRequest request);
}
