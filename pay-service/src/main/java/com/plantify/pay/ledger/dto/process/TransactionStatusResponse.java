package com.plantify.pay.ledger.dto.process;

import com.plantify.pay.ledger.domain.*;
import com.plantify.pay.account.domain.*;
import com.plantify.pay.point.domain.*;
import com.plantify.pay.settlement.domain.*;;

import java.time.LocalDateTime;

public record TransactionStatusResponse(
        Long transactionId,
        Long userId,
        Long sellerId,
        String orderId,
        String orderName,
        Status status,
        Long amount,
        String redirectUri,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long pointBalance,
        Long balance,
        String accountNum,
        BankName bankName
) {

    public static TransactionStatusResponse from(
            TransactionResponse tx,
            Pay pay,
            Point point,
            Account account
    ) {
        return new TransactionStatusResponse(
                tx.transactionId(),
                tx.userId(),
                tx.sellerId(),
                tx.orderId(),
                tx.orderName(),
                tx.status(),
                tx.amount(),
                tx.redirectUri(),
                tx.createdAt(),
                tx.updatedAt(),
                point.getPointBalance(),
                pay.getBalance(),
                account.getAccountNum(),
                account.getBankName()
        );
    }
}