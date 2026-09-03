package com.plantify.pay.ledger.application.event;

import com.plantify.pay.ledger.dto.process.TransactionStatusMessage;
import com.plantify.pay.ledger.application.ledger.LedgerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PayTransactionStatusServiceImpl implements PayTransactionStatusService {

    private final LedgerService ledgerService;

    private static final double POINT_REWARD_RATE = 0.005;

    // 성공
    @Override
    public void processSuccessfulTransaction(TransactionStatusMessage message) {

        long rewardPoints =
                Math.round(message.amount() * POINT_REWARD_RATE);

        ledgerService.reward(message.userId(), rewardPoints);
    }
}
