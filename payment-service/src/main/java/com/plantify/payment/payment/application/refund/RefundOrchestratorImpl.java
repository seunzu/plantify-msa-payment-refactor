package com.plantify.payment.payment.application.refund;

import com.plantify.payment.client.PayServiceClient;
import com.plantify.payment.client.TransactionServiceClient;
import com.plantify.payment.payment.dto.request.CreditRequest;
import com.plantify.payment.payment.dto.request.RefundRequest;
import com.plantify.payment.payment.dto.request.TransactionConfirmRequest;
import com.plantify.payment.payment.dto.response.PaymentResponse;
import com.plantify.payment.payment.domain.entity.Payment;
import com.plantify.payment.payment.domain.entity.Status;
import com.plantify.payment.global.exception.ApplicationException;
import com.plantify.payment.global.exception.errorcode.PaymentErrorCode;
import com.plantify.payment.global.exception.errorcode.RefundErrorCode;
import com.plantify.payment.global.util.UserInfoProvider;
import com.plantify.payment.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundOrchestratorImpl implements RefundOrchestrator {

    private final PaymentRepository paymentRepository;
    private final PayServiceClient payServiceClient;
    private final TransactionServiceClient transactionServiceClient;
    private final UserInfoProvider userInfoProvider;

    @Override
    @Transactional
    public PaymentResponse processRefund(RefundRequest request) {
        Long userId = userInfoProvider.getUserInfo().userId();

        // 환불 대상 Payment 조회 및 검증
        Payment payment = paymentRepository.findById(request.paymentId())
                .orElseThrow(() -> new ApplicationException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        // 본인 결제인지 확인
        if (!payment.getUserId().equals(userId)) {
            throw new ApplicationException(PaymentErrorCode.UNAUTHORIZED_ACCESS);
        }

        // APPROVED 상태만 환불 가능
        if (payment.getStatus() != Status.APPROVED) {
            throw new ApplicationException(RefundErrorCode.INVALID_REFUND_STATUS);
        }

        payment.updateReason(request.reason())
                .updateStatus(Status.REFUNDED);

        // Pay(Ledger) 잔액 복원
        try {
            payServiceClient.credit(
                    new CreditRequest(userId, payment.getAmount(), payment.getPointUsed())
            );
        } catch (Exception e) {
            log.error("잔액 복원 실패. paymentId={}, userId={}, amount={}, error={}",
                    payment.getPaymentId(), userId, payment.getAmount(), e.getMessage());
            throw new ApplicationException(PaymentErrorCode.LEDGER_CREDIT_FAILED);
        }

        // Transaction REFUNDED
        try {
            transactionServiceClient.refund(
                    new TransactionConfirmRequest(payment.getTransactionId(), payment.getPaymentId())
            );
        } catch (Exception e) {
            // 잔액은 이미 복원됨 — HTTP 호출이라 @Transactional 롤백 범위 밖
            // Payment는 REFUNDED로 확정하고, transaction 상태 불일치는 수동 처리 대상으로 기록
            log.error("환불 거래 확정 실패. 잔액은 복원됐으나 transaction 상태가 COMPLETED 유지. 수동 처리 필요. paymentId={}, transactionId={}, error={}",
                    payment.getPaymentId(), payment.getTransactionId(), e.getMessage());
        }

        // Payment REFUNDED (잔액 복원 완료 기준으로 확정)
        paymentRepository.save(payment);

        return PaymentResponse.from(payment);
    }
}
