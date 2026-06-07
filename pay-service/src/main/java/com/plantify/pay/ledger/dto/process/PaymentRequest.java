package com.plantify.pay.ledger.dto.process;

public record PaymentRequest(
        String orderId,
        String orderName,
        Long sellerId,
        Long amount,
        String redirectUri
) {}