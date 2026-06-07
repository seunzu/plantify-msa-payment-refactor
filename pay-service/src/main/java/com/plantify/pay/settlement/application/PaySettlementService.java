package com.plantify.pay.settlement.application;

import com.plantify.pay.settlement.dto.PaySettlementResponse;
import com.plantify.pay.ledger.domain.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface PaySettlementService {

    Page<PaySettlementResponse> getAllPaySettlements(Pageable pageable);
    Page<PaySettlementResponse> getPaySettlementByStatus(Status status, Pageable pageable);
    Long getPaySettlementAmount();

}