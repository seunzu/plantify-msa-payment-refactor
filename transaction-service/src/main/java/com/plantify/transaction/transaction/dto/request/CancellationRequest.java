package com.plantify.transaction.transaction.dto.request;

public record CancellationRequest(
        Long paymentId,
        String reason
) {
}
