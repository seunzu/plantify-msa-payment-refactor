package com.plantify.pay.ledger.application.facade;

import com.plantify.pay.ledger.dto.PayBalanceResponse;
import com.plantify.pay.ledger.dto.process.*;

public interface PayFacadeService {

    PaymentResponse createPayTransaction(PendingTransactionRequest request);
    TransactionStatusResponse getTransactionStatus(String token);
    ProcessPaymentResponse verifyAndProcessPayment(String token, Long pointToUse);
    ProcessPaymentResponse refund(UpdateTransactionRequest request);
    ProcessPaymentResponse cancellation(UpdateTransactionRequest request);
    PayBalanceResponse checkPayBalance(PayBalanceRequest request);
}