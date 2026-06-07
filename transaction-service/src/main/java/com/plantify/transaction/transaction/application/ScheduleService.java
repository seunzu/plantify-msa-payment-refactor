package com.plantify.transaction.transaction.application;

import com.plantify.transaction.transaction.domain.Status;
import com.plantify.transaction.transaction.domain.Transaction;
import com.plantify.transaction.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void markPendingTransactionsAsFailed() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(5);
        List<Transaction> expiredTransactions =
                transactionRepository.findAllByStatusAndCreatedAtBefore(
                        Status.PENDING, expirationTime
                );

        for (Transaction transaction : expiredTransactions) {
            transactionService.failExpiredTransaction(transaction.getTransactionId());
        }
    }
}
