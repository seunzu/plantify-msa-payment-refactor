package com.plantify.payment.payment.dto.request;

public record CancellationRequest(
        Long paymentId,
        String reason
) {
}