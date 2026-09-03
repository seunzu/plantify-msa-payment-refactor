package com.plantify.pay.settlement.application;

import com.plantify.pay.settlement.dto.PaySettlementRequest;
import com.plantify.pay.ledger.domain.Pay;
import com.plantify.pay.settlement.domain.PaySettlement;
import com.plantify.pay.ledger.domain.Status;
import com.plantify.pay.global.exception.ApplicationException;
import com.plantify.pay.global.exception.errorcode.PayErrorCode;
import com.plantify.pay.global.exception.errorcode.SettlementErrorCode;
import com.plantify.pay.ledger.repository.PayRepository;
import com.plantify.pay.settlement.repository.PaySettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaySettlementCommandServiceImpl implements PaySettlementCommandService {

    private final PaySettlementRepository paySettlementRepository;
    private final PayRepository payRepository;

    @Override
    @Transactional
    public void savePaySettlement(PaySettlementRequest request) {
        Long userId = request.userId();
        Pay pay = payRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException(PayErrorCode.PAY_NOT_FOUND));

        PaySettlement savedPaySettlement = request.toEntity(pay);
        paySettlementRepository.save(savedPaySettlement);
    }

    @Override
    public PaySettlement updateSettlementStatus(String orderId, Status status) {
        PaySettlement paySettlement = paySettlementRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ApplicationException(SettlementErrorCode.PAY_SETTLEMENT_NOT_FOUND))
                .updateStatus(status);
        return paySettlementRepository.save(paySettlement);
    }
}
