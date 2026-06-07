package com.plantify.payment.payment.application.refund;

import com.plantify.payment.payment.dto.response.ProcessResponse;

import java.util.List;

public interface RefundService {

    List<ProcessResponse> getAllRefunds();
    ProcessResponse getRefundById(Long paymentId);
    List<ProcessResponse> getRefundsByUserId(Long userId);
}
