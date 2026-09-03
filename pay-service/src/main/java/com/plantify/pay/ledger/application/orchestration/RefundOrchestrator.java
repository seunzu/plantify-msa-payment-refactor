package com.plantify.pay.ledger.application.orchestration;

import com.plantify.pay.client.TransactionServiceClient;
import com.plantify.pay.ledger.dto.process.ProcessPaymentResponse;
import com.plantify.pay.ledger.dto.process.TransactionResponse;
import com.plantify.pay.ledger.dto.process.UpdateTransactionRequest;
import com.plantify.pay.settlement.domain.PaySettlement;
import com.plantify.pay.ledger.domain.Status;
import com.plantify.pay.ledger.application.ledger.LedgerService;
import com.plantify.pay.settlement.application.PaySettlementCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundOrchestrator {

    private final LedgerService ledgerService;
    private final TransactionServiceClient transactionClient;
    private final PaySettlementCommandService paySettlementCommandService;

    @Transactional
    public ProcessPaymentResponse execute(UpdateTransactionRequest request) {

        TransactionResponse tx =
                transactionClient.updateTransactionToRefund(request).data();

        PaySettlement settlement =
                paySettlementCommandService.updateSettlementStatus(
                        request.orderId(), Status.REFUND
                );

        ledgerService.credit(
                tx.userId(),
                settlement.getAmount(),
                settlement.getPointUsed()
        );

        return ProcessPaymentResponse.from(tx);
    }
}
