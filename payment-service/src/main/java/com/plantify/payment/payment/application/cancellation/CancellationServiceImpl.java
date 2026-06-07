package com.plantify.payment.payment.application.cancellation;

import com.plantify.payment.payment.dto.response.ProcessResponse;
import com.plantify.payment.payment.domain.entity.Payment;
import com.plantify.payment.payment.domain.entity.Status;
import com.plantify.payment.global.exception.ApplicationException;
import com.plantify.payment.global.exception.errorcode.CancellationErrorCode;
import com.plantify.payment.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CancellationServiceImpl implements CancellationService {

    private final PaymentRepository paymentRepository;

    @Override
    public List<ProcessResponse> getAllCancellations() {
        return paymentRepository.findByStatus(Status.CANCELLATION)
                .stream()
                .map(ProcessResponse::from)
                .toList();
    }

    @Override
    public ProcessResponse getCancellationById(Long paymentId) {
        Payment payment = paymentRepository.findByPaymentIdAndStatus(paymentId, Status.CANCELLATION)
                .orElseThrow(() -> new ApplicationException(CancellationErrorCode.CANCELLATION_NOT_FOUND));
        return ProcessResponse.from(payment);
    }

    @Override
    public List<ProcessResponse> getCancellationsByUserId(Long userId) {
        return paymentRepository.findByUserIdAndStatus(userId, Status.CANCELLATION)
                .stream()
                .map(ProcessResponse::from)
                .toList();
    }
}
