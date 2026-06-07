package com.plantify.pay.ledger.dto.process;

public record PendingTransactionRequest(
        Long sellerId,
        String orderName,
        Long amount,
        String redirectUri
){
}
