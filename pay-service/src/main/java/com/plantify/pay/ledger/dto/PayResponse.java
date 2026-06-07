package com.plantify.pay.ledger.dto;

import com.plantify.pay.ledger.domain.Pay;

import java.time.LocalDateTime;

public record PayResponse(
        Long payNum,
        Long balance,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PayResponse from(Pay pay) {
        return new PayResponse(
                pay.getPayNum(),
                pay.getBalance(),
                pay.getCreatedAt(),
                pay.getUpdatedAt()
        );
    }
}