package com.plantify.payment.payment.application.payment;

import com.plantify.payment.client.PayServiceClient;
import com.plantify.payment.client.TransactionServiceClient;
import com.plantify.payment.payment.dto.request.CreditRequest;
import com.plantify.payment.payment.dto.request.DebitRequest;
import com.plantify.payment.payment.dto.request.PaymentInitRequest;
import com.plantify.payment.payment.dto.request.TransactionConfirmRequest;
import com.plantify.payment.payment.dto.request.TransactionRequest;
import com.plantify.payment.payment.dto.response.PaymentInitResponse;
import com.plantify.payment.payment.dto.response.PaymentResponse;
import com.plantify.payment.payment.dto.response.TransactionResponse;
import com.plantify.payment.payment.domain.entity.Method;
import com.plantify.payment.payment.domain.entity.Payment;
import com.plantify.payment.payment.domain.entity.Status;
import com.plantify.payment.payment.event.PaymentApprovedEvent;
import com.plantify.payment.payment.event.PaymentEventPublisher;
import com.plantify.payment.global.exception.ApplicationException;
import com.plantify.payment.global.exception.errorcode.PaymentErrorCode;
import com.plantify.payment.global.jwt.JwtProvider;
import com.plantify.payment.global.util.UserInfoProvider;
import com.plantify.payment.payment.repository.PaymentRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOrchestratorImpl implements PaymentOrchestrator {

    private final PaymentRepository paymentRepository;
    private final PayServiceClient payServiceClient;
    private final TransactionServiceClient transactionServiceClient;
    private final JwtProvider jwtProvider;
    private final UserInfoProvider userInfoProvider;
    private final PaymentEventPublisher paymentEventPublisher;

    // 결제 진입
    // Transaction PENDING 생성 + Payment PENDING 저장 + JWT(transactionId) 발급
    // 프론트) token + redirectUri 받아서 결제 페이지로 이동
    @Override
    @Transactional
    public PaymentInitResponse initiate(PaymentInitRequest request) {
        Long userId = userInfoProvider.getUserInfo().userId();

        TransactionResponse transaction;
        try {
            transaction = transactionServiceClient.createPendingTransaction(
                    TransactionRequest.from(request, userId)
            );
        } catch (FeignException.Conflict e) {
            log.warn("중복 결제 진입 시도. orderId={}, userId={}", request.orderId(), userId);
            throw new ApplicationException(PaymentErrorCode.DUPLICATE_PAYMENT);
        }

        // PENDING 저장 (transactionId 유니크 제약)
        Payment payment;
        try {
            payment = paymentRepository.save(
                    Payment.builder()
                            .userId(userId)
                            .transactionId(transaction.transactionId())
                            .orderId(transaction.orderId())
                            .orderName(transaction.orderName())
                            .amount(transaction.amount())
                            .pointUsed(0L)
                            .method(Method.PAY)
                            .status(Status.PENDING)
                            .build()
            );
        } catch (DataIntegrityViolationException e) {
            log.warn("중복 결제 진입 시도. transactionId={}, userId={}",
                    transaction.transactionId(), userId);
            throw new ApplicationException(PaymentErrorCode.DUPLICATE_PAYMENT);
        }

        // JWT 발급
        String token = jwtProvider.createPaymentToken(transaction.transactionId());

        return new PaymentInitResponse(
                payment.getPaymentId(),
                payment.getTransactionId(),
                payment.getOrderId(),
                payment.getOrderName(),
                payment.getAmount(),
                token,
                transaction.redirectUri()
        );
    }

    // 결제 실행
    // token 검증 -> transactionId 추출 -> Pay debit -> Transaction confirm
    @Override
    @Transactional
    public PaymentResponse processPayment(String token, Long pointToUse) {
        // token 검증 -> transactionId 추출
        Long transactionId = jwtProvider.getTransactionId(token);

        // transactionId로 Payment 조회
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ApplicationException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() != Status.PENDING) {
            throw new ApplicationException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }

        TransactionResponse transaction = transactionServiceClient.getTransactionById(transactionId);
        validatePendingTransaction(payment, transaction);

        payment.updatePointUsed(pointToUse);

        // Pay(Ledger) 잔액 차감
        try {
            payServiceClient.debit(
                    new DebitRequest(
                            payment.getTransactionId(),
                            payment.getUserId(),
                            payment.getOrderId(),
                            payment.getOrderName(),
                            payment.getAmount(),
                            payment.getPointUsed()
                    )
            );
        } catch (Exception e) {
            log.error("잔액 차감 실패. paymentId={}, transactionId={}, error={}",
                    payment.getPaymentId(), transactionId, e.getMessage());
            markPaymentFailedAndConfirmTransactionFailure(payment, "잔액 차감 처리에 실패했습니다.");
            throw new ApplicationException(PaymentErrorCode.LEDGER_DEBIT_FAILED);
        }

        // Transaction 상태 확정
        try {
            transactionServiceClient.confirm(
                    new TransactionConfirmRequest(transactionId, payment.getPaymentId())
            );
        } catch (Exception e) {
            log.error("거래 확정 실패. paymentId={}, transactionId={}, error={}",
                    payment.getPaymentId(), transactionId, e.getMessage());

            // 보상 트랜잭션: debit 된 잔액/포인트 복원
            try {
                payServiceClient.credit(
                        new CreditRequest(
                                payment.getUserId(),
                                payment.getAmount(),
                                payment.getPointUsed()
                        )
                );
                log.info("보상 트랜잭션(credit) 완료. paymentId={}, transactionId={}",
                        payment.getPaymentId(), transactionId);
            } catch (Exception creditEx) {
                // credit까지 실패한 경우 — 수동 처리 필요
                log.error("보상 트랜잭션(credit) 실패. 수동 처리 필요. paymentId={}, transactionId={}, error={}",
                        payment.getPaymentId(), transactionId, creditEx.getMessage());
            }

            markPaymentFailedAndConfirmTransactionFailure(payment, "거래 확정 처리에 실패했습니다.");
            throw new ApplicationException(PaymentErrorCode.TRANSACTION_CONFIRM_FAILED);
        }

        // Payment APPROVED
        payment.updateStatus(Status.APPROVED);
        paymentRepository.save(payment);
        paymentEventPublisher.publishApproved(PaymentApprovedEvent.from(payment));

        return PaymentResponse.from(payment);
    }

    private void validatePendingTransaction(Payment payment, TransactionResponse transaction) {
        if (!"PENDING".equals(transaction.status())) {
            throw new ApplicationException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }

        boolean mismatched = !payment.getUserId().equals(transaction.userId())
                || !payment.getOrderId().equals(transaction.orderId())
                || !payment.getAmount().equals(transaction.amount());

        if (mismatched) {
            throw new ApplicationException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    private void markPaymentFailedAndConfirmTransactionFailure(Payment payment, String reason) {
        payment.updateStatus(Status.FAILED);
        payment.updateReason(reason);
        paymentRepository.save(payment);

        try {
            transactionServiceClient.fail(
                    new TransactionConfirmRequest(payment.getTransactionId(), payment.getPaymentId())
            );
        } catch (Exception e) {
            log.error("거래 실패 확정 실패. 수동 처리 필요. paymentId={}, transactionId={}, error={}",
                    payment.getPaymentId(), payment.getTransactionId(), e.getMessage());
        }
    }
}
