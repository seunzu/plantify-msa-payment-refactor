package com.plantify.pay.ledger.application.orchestration;

import com.plantify.pay.client.TransactionServiceClient;
import com.plantify.pay.ledger.dto.process.*;
import com.plantify.pay.settlement.dto.PaySettlementRequest;
import com.plantify.pay.ledger.domain.Status;
import com.plantify.pay.config.JwtProvider;
import com.plantify.pay.ledger.application.ledger.LedgerService;
import com.plantify.pay.settlement.application.PaySettlementCommandService;
import com.plantify.pay.global.util.UserInfoProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentOrchestrator {

    private final LedgerService ledgerService;
    private final TransactionServiceClient transactionClient;
    private final PaySettlementCommandService paySettlementCommandService;
    private final JwtProvider jwtProvider;
    private final UserInfoProvider userInfoProvider;

    public PaymentResponse createPending(PendingTransactionRequest request) {
        Long userId = userInfoProvider.getUserInfo().userId();

        TransactionResponse tx =
                transactionClient.createPendingTransaction(
                        TransactionRequest.from(request, userId)
                ).data();

        String token = jwtProvider.createAccessToken(tx.transactionId());
        return PaymentResponse.from(tx, token, request.redirectUri());
    }

    public ProcessPaymentResponse execute(String token, long pointToUse) {

        Long transactionId = jwtProvider.getClaims(token).get("id", Long.class);
        TransactionResponse tx =
                transactionClient.getTransactionById(transactionId).data();

        long finalAmount = tx.amount() - pointToUse;
        boolean debited = false;
        boolean transactionConfirmed = false;

        try {
            ledgerService.debit(tx.userId(), finalAmount, pointToUse);
            debited = true;

            transactionClient.updateTransactionToSuccess(
                    new PayTransactionRequest(transactionId)
            );
            transactionConfirmed = true;
        } catch (Exception e) {
            if (debited && !transactionConfirmed) {
                log.warn("Payment compensation started. transactionId={}, userId={}",
                        transactionId, tx.userId(), e);
                ledgerService.credit(tx.userId(), finalAmount, pointToUse);
            }
            throw e;
        }

        paySettlementCommandService.savePaySettlement(
                new PaySettlementRequest(
                        tx.userId(),
                        tx.orderId(),
                        tx.orderName(),
                        tx.amount(),
                        Status.PAYMENT,
                        pointToUse
                )
        );

        return ProcessPaymentResponse.from(tx);
    }
}
