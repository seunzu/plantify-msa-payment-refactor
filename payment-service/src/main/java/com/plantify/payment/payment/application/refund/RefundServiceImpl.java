package com.plantify.payment.payment.application.refund;

import com.plantify.payment.payment.dto.response.ProcessResponse;
import com.plantify.payment.payment.domain.entity.Payment;
import com.plantify.payment.payment.domain.entity.Status;
import com.plantify.payment.global.exception.ApplicationException;
import com.plantify.payment.global.exception.errorcode.RefundErrorCode;
import com.plantify.payment.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final PaymentRepository paymentRepository;

    @Override
    public List<ProcessResponse> getAllRefunds() {
        return paymentRepository.findByStatus(Status.REFUND)
                .stream()
                .map(ProcessResponse::from)
                .toList();
    }

    @Override
    public ProcessResponse getRefundById(Long paymentId) {
        Payment payment = paymentRepository.findByPaymentIdAndStatus(paymentId, Status.REFUND)
                .orElseThrow(() -> new ApplicationException(RefundErrorCode.REFUND_NOT_FOUND));
        return ProcessResponse.from(payment);
    }


    @Override
    public List<ProcessResponse> getRefundsByUserId(Long userId) {
        return paymentRepository.findByUserIdAndStatus(userId, Status.REFUND)
                .stream()
                .map(ProcessResponse::from)
                .toList();
    }

}
