package com.plantify.pay.ledger.application.facade;

import com.plantify.pay.ledger.dto.PayBalanceResponse;
import com.plantify.pay.ledger.dto.process.*;
import com.plantify.pay.ledger.application.query.PayQueryService;
import com.plantify.pay.ledger.application.query.PaymentStatusQuery;
import com.plantify.pay.ledger.application.orchestration.CancellationOrchestrator;
import com.plantify.pay.ledger.application.orchestration.PaymentOrchestrator;
import com.plantify.pay.ledger.application.orchestration.RefundOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayFacadeServiceImpl implements PayFacadeService {

    private final PaymentStatusQuery paymentStatusQuery;
    private final PaymentOrchestrator paymentOrchestrator;
    private final RefundOrchestrator refundOrchestrator;
    private final CancellationOrchestrator cancellationOrchestrator;
    private final PayQueryService payQueryService;

    @Override
    public PaymentResponse createPayTransaction(PendingTransactionRequest request) {
        return paymentOrchestrator.createPending(request);
    }

    @Override
    public TransactionStatusResponse getTransactionStatus(String token) {
        return paymentStatusQuery.getStatus(token);
    }

    @Override
    public ProcessPaymentResponse verifyAndProcessPayment(String token, Long pointToUse) {
        return paymentOrchestrator.execute(token, pointToUse);
    }

    @Override
    public ProcessPaymentResponse refund(UpdateTransactionRequest request) {
        return refundOrchestrator.execute(request);
    }

    @Override
    public ProcessPaymentResponse cancellation(UpdateTransactionRequest request) {
        return cancellationOrchestrator.execute(request);
    }

    @Override
    public PayBalanceResponse checkPayBalance(PayBalanceRequest request) {
        return payQueryService.checkBalance(request);
    }
}
