package com.plantify.payment.payment.application.cancellation;

import com.plantify.payment.client.TransactionServiceClient;
import com.plantify.payment.payment.dto.request.CancellationRequest;
import com.plantify.payment.payment.dto.request.TransactionConfirmRequest;
import com.plantify.payment.payment.dto.response.PaymentResponse;
import com.plantify.payment.payment.domain.entity.Payment;
import com.plantify.payment.payment.domain.entity.Status;
import com.plantify.payment.global.exception.ApplicationException;
import com.plantify.payment.global.exception.errorcode.CancellationErrorCode;
import com.plantify.payment.global.exception.errorcode.PaymentErrorCode;
import com.plantify.payment.global.util.UserInfoProvider;
import com.plantify.payment.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancellationOrchestratorImpl implements CancellationOrchestrator {

    private final PaymentRepository paymentRepository;
    private final TransactionServiceClient transactionServiceClient;
    private final UserInfoProvider userInfoProvider;

    @Override
    @Transactional
    public PaymentResponse processCancellation(CancellationRequest request) {
        Long userId = userInfoProvider.getUserInfo().userId();

        // 취소 대상 Payment 조회 및 검증
        Payment payment = paymentRepository.findById(request.paymentId())
                .orElseThrow(() -> new ApplicationException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        // 본인 결제인지 확인
        if (!payment.getUserId().equals(userId)) {
            throw new ApplicationException(PaymentErrorCode.UNAUTHORIZED_ACCESS);
        }

        // PENDING 상태만 취소 가능
        if (payment.getStatus() != Status.PENDING) {
            throw new ApplicationException(CancellationErrorCode.INVALID_CANCELLATION_STATUS);
        }

        payment.updateReason(request.reason())
                .updateStatus(Status.CANCELLED);

        // Transaction CANCELLED (Ledger 호출 X)
        try {
            transactionServiceClient.cancel(
                    new TransactionConfirmRequest(payment.getTransactionId(), payment.getPaymentId())
            );
        } catch (Exception e) {
            log.error("취소 거래 확정 실패. paymentId={}, transactionId={}, error={}",
                    payment.getPaymentId(), payment.getTransactionId(), e.getMessage());
            throw new ApplicationException(CancellationErrorCode.INTERNAL_SERVER_ERROR);
        }

        // Payment CANCELLED
        paymentRepository.save(payment);

        return PaymentResponse.from(payment);
    }
}
