package com.plantify.pay.settlement.application;

import com.plantify.pay.settlement.dto.PaySettlementRequest;
import com.plantify.pay.settlement.domain.PaySettlement;
import com.plantify.pay.ledger.domain.Status;

public interface PaySettlementCommandService {

    void savePaySettlement(PaySettlementRequest request);
    PaySettlement updateSettlementStatus(String orderId, Status status);
}
