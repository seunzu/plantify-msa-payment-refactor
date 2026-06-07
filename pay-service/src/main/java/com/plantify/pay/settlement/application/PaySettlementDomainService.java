package com.plantify.pay.settlement.application;

import com.plantify.pay.ledger.dto.ExternalSettlementResponse;
import com.plantify.pay.settlement.dto.PaySettlementRequest;
import com.plantify.pay.settlement.domain.PaySettlement;
import com.plantify.pay.ledger.domain.Status;

public interface PaySettlementDomainService {

    void savePaySettlement(PaySettlementRequest request);
    ExternalSettlementResponse getSettlementByOrderId(String orderId);
    PaySettlement updateSettlementStatus(String orderId, Status status);
}
