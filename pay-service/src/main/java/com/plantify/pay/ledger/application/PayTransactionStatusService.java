package com.plantify.pay.ledger.application;

import com.plantify.pay.ledger.dto.process.PaymentApprovedEvent;

public interface PayTransactionStatusService {

    void processApprovedPayment(PaymentApprovedEvent event);
}
