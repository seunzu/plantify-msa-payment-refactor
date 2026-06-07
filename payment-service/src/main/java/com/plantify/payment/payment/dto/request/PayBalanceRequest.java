package com.plantify.payment.payment.dto.request;

public record PayBalanceRequest(
        Long userId,
        Long amount
) {
}