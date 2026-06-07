package com.plantify.pay.settlement.dto;

import com.plantify.pay.ledger.domain.Pay;
import com.plantify.pay.settlement.domain.PaySettlement;
import com.plantify.pay.ledger.domain.Status;

public record PaySettlementRequest(
        Long transactionId,
        Long userId,
        String orderId,
        String orderName,
        Long amount,
        Status status,
        Long pointUsed
) {

    public PaySettlement toEntity(Pay pay) {
        return PaySettlement.builder()
                .pay(pay)
                .transactionId(transactionId)
                .status(status)
                .orderId(orderId)
                .orderName(orderName)
                .amount(amount)
                .pointUsed(pointUsed)
                .build();
    }
}