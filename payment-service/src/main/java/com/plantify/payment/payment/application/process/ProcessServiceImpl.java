package com.plantify.payment.payment.application.process;

import com.plantify.payment.client.PayServiceClient;
import com.plantify.payment.payment.dto.request.CancellationRequest;
import com.plantify.payment.payment.dto.request.PayBalanceRequest;
import com.plantify.payment.payment.dto.request.PaymentRequest;
import com.plantify.payment.payment.dto.request.RefundRequest;
import com.plantify.payment.payment.dto.response.ProcessResponse;
import com.plantify.payment.payment.domain.entity.Payment;
import com.plantify.payment.payment.domain.entity.Status;
import com.plantify.payment.global.exception.ApplicationException;
import com.plantify.payment.global.exception.errorcode.CancellationErrorCode;
import com.plantify.payment.global.exception.errorcode.PaymentErrorCode;
import com.plantify.payment.global.exception.errorcode.RefundErrorCode;
import com.plantify.payment.global.util.LockProvider;
import com.plantify.payment.global.util.UserInfoProvider;
import com.plantify.payment.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class ProcessServiceImpl implements ProcessService {

    private final PaymentRepository paymentRepository;
    private final LockProvider lockProvider;
    private final PayServiceClient payServiceClient;
    private final UserInfoProvider userInfoProvider;

    @Override
    @Transactional
    public ProcessResponse processPayment(PaymentRequest request) {

        Long userId = userInfoProvider.getUserInfo().userId();

        return withPaymentLock(userId, () -> {

            Payment payment = paymentRepository.save(
                    request.toEntity(userId)
            );

            try {
                payServiceClient.checkPayBalance(
                        new PayBalanceRequest(userId, request.amount())
                );
                payment.updateStatus(Status.PAYMENT);

            } catch (ApplicationException ex) {
                if (ex.getErrorCode().getHttpStatus() == HttpStatus.BAD_REQUEST) {
                    payment.updateStatus(Status.FAILED);
                } else {
                    throw ex;
                }
            }

            paymentRepository.save(payment);
            return ProcessResponse.from(payment);
        });
    }

    @Override
    @Transactional
    public ProcessResponse processCancellation(CancellationRequest request) {

        Long userId = userInfoProvider.getUserInfo().userId();

        return withPaymentLock(userId, () -> {

            Payment payment = paymentRepository.findById(request.paymentId())
                    .orElseThrow(() ->
                            new ApplicationException(PaymentErrorCode.PAYMENT_NOT_FOUND));

            if (payment.getStatus() != Status.PENDING) {
                throw new ApplicationException(
                        CancellationErrorCode.INVALID_CANCELLATION_STATUS);
            }

            payment.updateReason(request.reason())
                    .updateStatus(Status.CANCELLATION);

            paymentRepository.save(payment);
            return ProcessResponse.from(payment);
        });
    }

    @Override
    @Transactional
    public ProcessResponse processRefund(RefundRequest request) {

        Long userId = userInfoProvider.getUserInfo().userId();

        return withPaymentLock(userId, () -> {

            Payment payment = paymentRepository.findById(request.paymentId())
                    .orElseThrow(() ->
                            new ApplicationException(PaymentErrorCode.PAYMENT_NOT_FOUND));

            if (payment.getStatus() != Status.PAYMENT) {
                throw new ApplicationException(
                        RefundErrorCode.INVALID_REFUND_STATUS);
            }

            payment.updateReason(request.reason())
                    .updateStatus(Status.REFUND);

            paymentRepository.save(payment);
            return ProcessResponse.from(payment);
        });
    }

    private <T> T withPaymentLock(Long userId, Supplier<T> action) {
        RLock lock = lockProvider.getPaymentLock(userId);
        boolean locked = false;

        try {
            locked = lock.tryLock(1, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new ApplicationException(PaymentErrorCode.CONCURRENT_UPDATE);
            }
            return action.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApplicationException(PaymentErrorCode.CONCURRENT_UPDATE);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }
}
