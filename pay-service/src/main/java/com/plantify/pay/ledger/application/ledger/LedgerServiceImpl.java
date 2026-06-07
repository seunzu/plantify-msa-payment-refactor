package com.plantify.pay.ledger.application.ledger;

import com.plantify.pay.ledger.domain.Pay;
import com.plantify.pay.global.exception.ApplicationException;
import com.plantify.pay.global.exception.errorcode.PayErrorCode;
import com.plantify.pay.global.util.LockProvider;
import com.plantify.pay.ledger.repository.PayRepository;
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
    private final PointDomainService pointService;
    private final LockProvider lockProvider;

    // 결제/포인트 차감
    @Override
    @Transactional
    public void debit(Long userId, long amount, long pointToUse) {
        withLedgerLock(userId, () -> {
            Pay pay = payRepository.findByUserId(userId)
                    .orElseThrow(() -> new ApplicationException(PayErrorCode.PAY_NOT_FOUND))
                    .validatePay(amount)
                    .success(amount);

            payRepository.save(pay);

            if (pointToUse > 0) {
                pointService.usePoints(userId, pointToUse);
            }
        });
    }

    // 환불/적립
    @Override
    @Transactional
    public void credit(Long userId, long amount, long point) {
        withLedgerLock(userId, () -> {
            Pay pay = payRepository.findByUserId(userId)
                    .orElseThrow(() -> new ApplicationException(PayErrorCode.PAY_NOT_FOUND))
                    .updatedBalance(amount);

            payRepository.save(pay);

            if (point > 0) {
                pointService.addPoints(userId, point);
            }
        });
    }

    @Override
    @Transactional
    public void reward(Long userId, long rewardPoint) {
        withLedgerLock(userId, () -> {
            if (rewardPoint > 0) {
                pointService.addPoints(userId, rewardPoint);
            }
        });
    }

    @Override
    @Transactional
    public void fail(Long userId) {
        withLedgerLock(userId, () -> {
            Pay pay = payRepository.findByUserId(userId)
                    .orElseThrow(() -> new ApplicationException(PayErrorCode.PAY_NOT_FOUND))
                    .fail();

            payRepository.save(pay);
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
