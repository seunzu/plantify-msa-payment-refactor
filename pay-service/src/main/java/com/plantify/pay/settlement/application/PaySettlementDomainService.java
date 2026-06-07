package com.plantify.pay.settlement.application;

import com.plantify.pay.ledger.dto.ExternalSettlementResponse;
import com.plantify.pay.settlement.dto.PaySettlementRequest;
import com.plantify.pay.settlement.domain.PaySettlement;
import com.plantify.pay.ledger.domain.Status;

public interface PaySettlementDomainService {

    boolean existsByTransactionId(Long transactionId);
    boolean existsByTransactionIdAndStatus(Long transactionId, Status status);
    void savePaySettlement(PaySettlementRequest request);
    void updateSettlementStatusByTransactionId(Long transactionId, Status status);
    ExternalSettlementResponse getSettlementByOrderId(String orderId);
    PaySettlement updateSettlementStatus(String orderId, Status status);
}
