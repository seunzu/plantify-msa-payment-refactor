package com.plantify.transaction.transaction.application;

import com.plantify.transaction.transaction.dto.TransactionStatusMessage;
import com.plantify.transaction.transaction.domain.Status;
import com.plantify.transaction.transaction.domain.Transaction;
import com.plantify.transaction.global.exception.ApplicationException;
import com.plantify.transaction.global.exception.errorcode.TransactionErrorCode;
import com.plantify.transaction.global.util.LockProvider;
import com.plantify.transaction.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TransactionStatusServiceImpl implements TransactionStatusService {

    private final TransactionRepository transactionRepository;
    private final LockProvider lockProvider;

    // 성공
    @Override
    @Transactional
    public void processSuccessfulTransaction(TransactionStatusMessage message) {

        RLock lock = lockProvider.getUserLock(message.userId());
        boolean locked = false;

        try {
            locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new ApplicationException(TransactionErrorCode.CONCURRENT_UPDATE);
            }

            Transaction transaction = transactionRepository.findById(message.transactionId())
                    .orElseThrow(() -> new ApplicationException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

            transaction.updateStatus(message.status());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApplicationException(TransactionErrorCode.CONCURRENT_UPDATE);
        } finally {
            if (locked) lock.unlock();
        }
    }

    // 실패
    @Override
    @Transactional
    public void processFailedTransaction(TransactionStatusMessage message) {

        RLock lock = lockProvider.getUserLock(message.userId());
        boolean locked = false;

        try {
            locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new ApplicationException(TransactionErrorCode.CONCURRENT_UPDATE);
            }

            Transaction transaction = transactionRepository.findById(message.transactionId())
                    .orElseThrow(() -> new ApplicationException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

            transaction.updateStatus(Status.FAILED);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApplicationException(TransactionErrorCode.CONCURRENT_UPDATE);
        } finally {
            if (locked) lock.unlock();
        }
    }
}