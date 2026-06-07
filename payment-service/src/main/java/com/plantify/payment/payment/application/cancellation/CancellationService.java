package com.plantify.payment.payment.application.cancellation;

import com.plantify.payment.payment.dto.response.ProcessResponse;

import java.util.List;

public interface CancellationService {

    List<ProcessResponse> getAllCancellations();
    ProcessResponse getCancellationById(Long cancellationId);
    List<ProcessResponse> getCancellationsByUserId(Long userId);
}
