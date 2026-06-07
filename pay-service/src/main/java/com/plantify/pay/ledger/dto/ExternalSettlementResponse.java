package com.plantify.pay.ledger.dto;

import com.plantify.pay.settlement.domain.PaySettlement;

import java.time.LocalDateTime;

public record ExternalSettlementResponse(
        String orderId,
        String orderName,
        LocalDateTime createdAt,
        Long amount
) {

    public static ExternalSettlementResponse from(PaySettlement paySettlement) {
        return new ExternalSettlementResponse(
                paySettlement.getOrderId(),
                paySettlement.getOrderName(),
                paySettlement.getCreatedAt(),
                paySettlement.getAmount()
        );
    }
}
