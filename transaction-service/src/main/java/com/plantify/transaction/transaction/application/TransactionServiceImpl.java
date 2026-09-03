package com.plantify.transaction.transaction.application;

import com.plantify.transaction.client.PaymentServiceClient;
import com.plantify.transaction.transaction.dto.*;;
import com.plantify.transaction.transaction.dto.request.*;
import com.plantify.transaction.transaction.dto.response.ProcessResponse;
import com.plantify.transaction.transaction.dto.response.TransactionResponse;
import com.plantify.transaction.transaction.domain.Status;
import com.plantify.transaction.transaction.domain.Transaction;
import com.plantify.transaction.global.exception.ApplicationException;
import com.plantify.transaction.global.exception.errorcode.TransactionErrorCode;
import com.plantify.transaction.global.util.LockProvider;
import com.plantify.transaction.kafka.TransactionProvider;
import com.plantify.transaction.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final PaymentServiceClient paymentServiceClient;
    private final TransactionProvider transactionProvider;
    private final LockProvider lockProvider;

    @Override
    public TransactionResponse getTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new ApplicationException(TransactionErrorCode.TRANSACTION_NOT_FOUND));
        return TransactionResponse.from(transaction);
    }

    // PENDING 트랜잭션
    @Override
    @Transactional
    public TransactionResponse createPendingTransaction(TransactionRequest request) {
        Transaction transaction = withTransactionLock(request.userId(), () ->
                transactionRepository.save(request.toEntity())
        );

        return TransactionResponse.from(transaction);
    }

    // 결제 성공 (PENDING -> PAYMENT)
    @Override
    @Transactional
    public TransactionResponse updateTransactionToSuccess(PayTransactionRequest request) {

        Transaction transaction = transactionRepository.findById(request.transactionId())
                .orElseThrow(() ->
                        new ApplicationException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

        withTransactionLock(transaction.getUserId(), () -> {

            if (transaction.getStatus() != Status.PENDING) {
                return;
            }

            PaymentRequest paymentRequest = new PaymentRequest(
                    transaction.getTransactionId(),
                    transaction.getOrderId(),
                    transaction.getOrderName(),
                    transaction.getAmount()
            );

            ProcessResponse response =
                    paymentServiceClient.processPayment(paymentRequest).data();

            if (response.status() != Status.PAYMENT) {
                log.error("Invalid payment result. transactionId={}, status={}",
                        transaction.getTransactionId(),
                        response.status());
                throw new ApplicationException(TransactionErrorCode.TRANSACTION_FAILED);
            }

            transaction.updateStatus(Status.PAYMENT)
                    .updatePaymentId(response.paymentId());

            transactionRepository.save(transaction);

            transactionProvider.sendTransactionStatusMessage(
                    TransactionStatusMessage.from(transaction)
            );
        });

        return TransactionResponse.from(transaction);
    }

    // 환불 처리 (PAYMENT -> REFUND)
    @Override
    @Transactional
    public TransactionResponse updateTransactionToRefund(UpdateTransactionRequest request) {

        Transaction transaction = transactionRepository.findByOrderId(request.orderId())
                .orElseThrow(() ->
                        new ApplicationException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

        withTransactionLock(transaction.getUserId(), () -> {

            if (transaction.getStatus() != Status.PAYMENT) {
                throw new ApplicationException(TransactionErrorCode.INVALID_TRANSACTION_STATUS);
            }

            RefundRequest refundRequest = new RefundRequest(
                    transaction.getPaymentId(),
                    request.reason()
            );

            paymentServiceClient.processRefund(refundRequest);

            transaction.updateStatus(Status.REFUND)
                    .updateReason(request.reason());

            transactionRepository.save(transaction);

            transactionProvider.sendTransactionStatusMessage(
                    TransactionStatusMessage.from(transaction)
            );
        });

        return TransactionResponse.from(transaction);
    }

    // 결제 취소 (PENDING -> CANCELLATION)
    @Override
    @Transactional
    public TransactionResponse updateTransactionToCancellation(UpdateTransactionRequest request) {

        Transaction transaction = transactionRepository.findByOrderId(request.orderId())
                .orElseThrow(() ->
                        new ApplicationException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

        withTransactionLock(transaction.getUserId(), () -> {

            if (transaction.getStatus() != Status.PENDING) {
                throw new ApplicationException(TransactionErrorCode.INVALID_TRANSACTION_STATUS);
            }

            CancellationRequest cancellationRequest = new CancellationRequest(
                    transaction.getPaymentId(),
                    request.reason()
            );

            paymentServiceClient.processCancellation(cancellationRequest);

            transaction.updateStatus(Status.CANCELLATION)
                    .updateReason(request.reason());

            transactionRepository.save(transaction);

            transactionProvider.sendTransactionStatusMessage(
                    TransactionStatusMessage.from(transaction)
            );
        });

        return TransactionResponse.from(transaction);
    }

    // 스케줄러용 만료 처리 (PENDING -> FAILED)
    @Override
    @Transactional
    public void failExpiredTransaction(Long transactionId) {

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new ApplicationException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

        withTransactionLock(transaction.getUserId(), () -> {

            if (transaction.getStatus() != Status.PENDING) {
                return;
            }

            transaction.updateStatus(Status.FAILED);
            transactionRepository.save(transaction);

            transactionProvider.sendTransactionStatusMessage(
                    TransactionStatusMessage.from(transaction)
            );

        });
    }

    private void withTransactionLock(Long userId, Runnable action) {
        RLock lock = lockProvider.getUserLock(userId);
        boolean locked = false;

        try {
            locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new ApplicationException(TransactionErrorCode.CONCURRENT_UPDATE);
            }
            action.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApplicationException(TransactionErrorCode.CONCURRENT_UPDATE);
        } finally {
            if (locked) lock.unlock();
        }
    }

    private <T> T withTransactionLock(Long userId, Supplier<T> action) {
        RLock lock = lockProvider.getUserLock(userId);
        boolean locked = false;

        try {
            locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new ApplicationException(TransactionErrorCode.CONCURRENT_UPDATE);
            }
            return action.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApplicationException(TransactionErrorCode.CONCURRENT_UPDATE);
        } finally {
            if (locked) lock.unlock();
        }
    }
}
