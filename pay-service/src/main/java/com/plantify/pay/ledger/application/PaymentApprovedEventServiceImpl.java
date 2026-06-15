package com.plantify.pay.ledger.application;

import com.plantify.pay.ledger.dto.process.PaymentApprovedEvent;
import com.plantify.pay.ledger.domain.Status;
import com.plantify.pay.settlement.application.PaySettlementDomainService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentApprovedEventServiceImpl implements PaymentApprovedEventService {

    private final LedgerService ledgerService;
    private final PaySettlementDomainService paySettlementDomainService;

    private static final double POINT_REWARD_RATE = 0.005; // 0.5%

    @Override
    @Transactional
    public void processApprovedPayment(PaymentApprovedEvent event) {
        if (paySettlementDomainService.existsByTransactionIdAndStatus(
                event.transactionId(), Status.REWARDED)) {
            log.info("이미 적립된 포인트. skip. transactionId={}", event.transactionId());
            return;
        }

        long rewardPoints = Math.round(event.amount() * POINT_REWARD_RATE);
        log.info("포인트 적립. transactionId={}, amount={}, rewardPoints={}",
                event.transactionId(), event.amount(), rewardPoints);
        ledgerService.reward(event.userId(), rewardPoints);

        paySettlementDomainService.updateSettlementStatusByTransactionId(
                event.transactionId(), Status.REWARDED
        );
    }
}
