package com.plantify.pay.settlement.dto;

import com.plantify.pay.settlement.domain.PaySettlement;
import com.plantify.pay.ledger.domain.Status;

import java.time.LocalDateTime;

public record PaySettlementResponse(
        Long paySettlementId,
        Status status,
        Long amount,
        String orderId,
        String orderName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PaySettlementResponse from(PaySettlement paySettlement) {
        return new PaySettlementResponse(
                paySettlement.getPaySettlementId(),
                paySettlement.getStatus(),
                paySettlement.getAmount(),
                paySettlement.getOrderId(),
                paySettlement.getOrderName(),
                paySettlement.getCreatedAt(),
                paySettlement.getUpdatedAt()
        );
    }
}