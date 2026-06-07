package com.plantify.transaction.transaction.dto.request;

public record RefundRequest (
        Long paymentId,
        String reason
)  {
}
