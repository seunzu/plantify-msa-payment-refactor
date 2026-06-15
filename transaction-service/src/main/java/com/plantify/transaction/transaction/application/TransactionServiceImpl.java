package com.plantify.transaction.transaction.application;

import com.plantify.transaction.transaction.dto.request.TransactionConfirmRequest;
import com.plantify.transaction.transaction.dto.request.TransactionRequest;
import com.plantify.transaction.transaction.dto.response.TransactionResponse;
import com.plantify.transaction.transaction.domain.Status;
import com.plantify.transaction.transaction.domain.Transaction;
import com.plantify.transaction.global.exception.ApplicationException;
import com.plantify.transaction.global.exception.errorcode.TransactionErrorCode;
import com.plantify.transaction.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public TransactionResponse getTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ApplicationException(TransactionErrorCode.TRANSACTION_NOT_FOUND));
        return TransactionResponse.from(transaction);
    }

    @Override
    public boolean existTransaction(Long userId, String orderId, List<Status> statuses) {
        return transactionRepository.existsByUserIdAndOrderIdAndStatusIn(userId, orderId, statuses);
    }

    // PENDING 생성 — orderId 유니크 제약으로 중복 방지
    @Override
    @Transactional
    public TransactionResponse createPendingTransaction(TransactionRequest request) {
        Transaction transaction;
        try {
            transaction = transactionRepository.save(request.toEntity());
        } catch (DataIntegrityViolationException e) {
            log.warn("중복 트랜잭션 생성 시도. orderId={}, userId={}", request.orderId(), request.userId());
            throw new ApplicationException(TransactionErrorCode.DUPLICATE_TRANSACTION);
        }

        return TransactionResponse.from(transaction);
    }

    // PENDING → COMPLETED
    // Payment Orchestrator가 Ledger 차감 완료 후 호출
    // Transaction은 상태 전이 + paymentId 기록만 수행
    @Override
    @Transactional
    public TransactionResponse confirmPayment(TransactionConfirmRequest request) {
        Transaction transaction = transactionRepository.findById(request.transactionId())
                .orElseThrow(() -> new ApplicationException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

        if (transaction.getStatus() != Status.PENDING) {
            throw new ApplicationException(TransactionErrorCode.INVALID_TRANSACTION_STATUS);
        }

        transaction.updateStatus(Status.COMPLETED)
                .updatePaymentId(request.paymentId());

        transactionRepository.save(transaction);

        return TransactionResponse.from(transaction);
    }

    // COMPLETED → REFUNDED
    // Payment Orchestrator가 Ledger 복원 완료 후 호출
    @Override
    @Transactional
    public TransactionResponse confirmRefund(TransactionConfirmRequest request) {
        Transaction transaction = transactionRepository.findById(request.transactionId())
                .orElseThrow(() -> new ApplicationException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

        if (transaction.getStatus() != Status.COMPLETED) {
            throw new ApplicationException(TransactionErrorCode.INVALID_TRANSACTION_STATUS);
        }

        transaction.updateStatus(Status.REFUNDED);

        transactionRepository.save(transaction);

        return TransactionResponse.from(transaction);
    }

    // PENDING → CANCELLED
    // Ledger 호출 없이 상태 전이만 수행 — PENDING 단계에서는 금전 변경 없음
    @Override
    @Transactional
    public TransactionResponse confirmCancellation(TransactionConfirmRequest request) {
        Transaction transaction = transactionRepository.findById(request.transactionId())
                .orElseThrow(() -> new ApplicationException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

        if (transaction.getStatus() != Status.PENDING) {
            throw new ApplicationException(TransactionErrorCode.INVALID_TRANSACTION_STATUS);
        }

        transaction.updateStatus(Status.CANCELLED);

        transactionRepository.save(transaction);

        return TransactionResponse.from(transaction);
    }

    // PENDING -> FAILED
    // Payment Orchestrator가 결제 대기 만료를 판단한 뒤 호출
    @Override
    @Transactional
    public TransactionResponse confirmFailure(TransactionConfirmRequest request) {
        Transaction transaction = transactionRepository.findById(request.transactionId())
                .orElseThrow(() -> new ApplicationException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

        if (transaction.getStatus() != Status.PENDING) {
            throw new ApplicationException(TransactionErrorCode.INVALID_TRANSACTION_STATUS);
        }

        transaction.updateStatus(Status.FAILED);
        transactionRepository.save(transaction);

        return TransactionResponse.from(transaction);
    }
}
