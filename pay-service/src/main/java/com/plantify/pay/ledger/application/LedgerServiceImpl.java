package com.plantify.pay.ledger.application;

import com.plantify.pay.ledger.dto.CreditRequest;
import com.plantify.pay.ledger.dto.DebitRequest;
import com.plantify.pay.ledger.domain.Pay;
import com.plantify.pay.ledger.domain.Status;
import com.plantify.pay.settlement.dto.PaySettlementRequest;
import com.plantify.pay.global.exception.ApplicationException;
import com.plantify.pay.global.exception.errorcode.PayErrorCode;
import com.plantify.pay.global.util.LockProvider;
import com.plantify.pay.ledger.repository.PayRepository;
import com.plantify.pay.settlement.application.PaySettlementDomainService;
import com.plantify.pay.point.application.PointDomainService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LedgerServiceImpl implements LedgerService {

    private final PayRepository payRepository;
    private final PaySettlementDomainService paySettlementDomainService;
    private final PointDomainService pointService;
    private final LockProvider lockProvider;

    // 결제 시 잔액 차감 + 포인트 차감
    // 정산 기록은 PaySettlementDomainService에 위임
    @Override
    @Transactional
    public void debit(DebitRequest request) {
        withLedgerLock(request.userId(), () -> {

            // transactionId 기반 멱등성: 이미 처리된 요청이면 skip
            if (paySettlementDomainService.existsByTransactionId(request.transactionId())) {
                return;
            }

            Pay pay = payRepository.findByUserId(request.userId())
                    .orElseThrow(() -> new ApplicationException(PayErrorCode.PAY_NOT_FOUND))
                    .validatePay(request.amount())
                    .success(request.amount());

            payRepository.save(pay);

            if (request.pointToUse() > 0) {
                pointService.usePoints(request.userId(), request.pointToUse());
            }

            paySettlementDomainService.savePaySettlement(
                    new PaySettlementRequest(
                            request.transactionId(),
                            request.userId(),
                            request.orderId(),
                            request.orderName(),
                            request.amount(),
                            Status.APPROVED,
                            request.pointToUse()
                    )
            );
        });
    }

    // 환불 시 잔액 복원 + 포인트 복환
    @Override
    @Transactional
    public void credit(CreditRequest request) {
        withLedgerLock(request.userId(), () -> {
            Pay pay = payRepository.findByUserId(request.userId())
                    .orElseThrow(() -> new ApplicationException(PayErrorCode.PAY_NOT_FOUND))
                    .updatedBalance(request.amount());

            payRepository.save(pay);

            if (request.point() > 0) {
                pointService.addPoints(request.userId(), request.point());
            }
        });
    }

    // 결제 완료 후 포인트 적립 (Kafka COMPLETED 이벤트 수신)
    @Override
    @Transactional
    public void reward(Long userId, long rewardPoint) {
        withLedgerLock(userId, () -> {
            if (rewardPoint > 0) {
                pointService.addPoints(userId, rewardPoint);
            }
        });
    }

    private void withLedgerLock(Long userId, Runnable action) {
        RLock lock = lockProvider.getLedgerLock(userId);
        boolean locked = false;

        try {
            locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new ApplicationException(PayErrorCode.CONCURRENT_UPDATE);
            }
            action.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApplicationException(PayErrorCode.CONCURRENT_UPDATE);
        } finally {
            if (locked) lock.unlock();
        }
    }
}
