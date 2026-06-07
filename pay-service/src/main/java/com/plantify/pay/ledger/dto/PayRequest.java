package com.plantify.pay.ledger.dto;

public record PayRequest(
        Long accountId,
        Long balance
) {
}