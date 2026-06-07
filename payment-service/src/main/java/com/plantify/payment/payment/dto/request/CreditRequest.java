package com.plantify.payment.payment.dto.request;

public record CreditRequest(
        Long userId,
        Long amount,
        Long point
) {
}