package com.plantify.payment.payment.repository;

import com.plantify.payment.payment.domain.entity.Payment;
import com.plantify.payment.payment.domain.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByStatus(Status status);
    Optional<Payment> findByPaymentIdAndStatus (Long paymentId, Status status);
    Optional<Payment> findByUserIdAndStatus(Long userId, Status status);
}
