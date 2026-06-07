package com.plantify.payment.payment.dto.request;

public record RefundRequest (
        Long paymentId,
        String reason
) {
}
