package com.plantify.payment.payment.application.payment;

import com.plantify.payment.client.TransactionServiceClient;
import com.plantify.payment.payment.domain.entity.Payment;
import com.plantify.payment.payment.domain.entity.Status;
import com.plantify.payment.payment.dto.request.TransactionConfirmRequest;
import com.plantify.payment.payment.dto.response.TransactionResponse;
import com.plantify.payment.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentExpirationScheduler {

    private final PaymentRepository paymentRepository;
    private final TransactionServiceClient transactionServiceClient;

    @Value("${payment.expiration.pending-minutes}")
    private long pendingExpirationMinutes;

    @Scheduled(fixedDelayString = "${payment.expiration.schedule-delay-ms}")
    @Transactional
    public void failExpiredPendingPayments() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(pendingExpirationMinutes);
        List<Payment> expiredPayments = paymentRepository.findAllByStatusAndCreatedAtBefore(
                Status.PENDING, expirationTime
        );

        for (Payment payment : expiredPayments) {
            failExpiredPayment(payment);
        }
    }

    private void failExpiredPayment(Payment payment) {
        try {
            TransactionResponse transaction =
                    transactionServiceClient.getTransactionById(payment.getTransactionId());

            if (!"PENDING".equals(transaction.status())) {
                log.warn("만료 Payment와 Transaction 상태 불일치. paymentId={}, transactionId={}, transactionStatus={}",
                        payment.getPaymentId(), payment.getTransactionId(), transaction.status());
                if ("FAILED".equals(transaction.status())) {
                    payment.updateStatus(Status.FAILED);
                    payment.updateReason("결제 대기 시간이 만료되었습니다.");
                    paymentRepository.save(payment);
                }
                return;
            }

            transactionServiceClient.fail(
                    new TransactionConfirmRequest(payment.getTransactionId(), payment.getPaymentId())
            );

            payment.updateStatus(Status.FAILED);
            payment.updateReason("결제 대기 시간이 만료되었습니다.");
            paymentRepository.save(payment);

            log.info("만료 결제 실패 처리 완료. paymentId={}, transactionId={}",
                    payment.getPaymentId(), payment.getTransactionId());
        } catch (Exception e) {
            log.error("만료 결제 실패 처리 중 오류. paymentId={}, transactionId={}, error={}",
                    payment.getPaymentId(), payment.getTransactionId(), e.getMessage());
        }
    }
}
