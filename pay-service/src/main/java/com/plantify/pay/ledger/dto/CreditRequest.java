package com.plantify.pay.ledger.dto;

public record CreditRequest(
        Long userId,
        Long amount,
        Long point
) {
}
