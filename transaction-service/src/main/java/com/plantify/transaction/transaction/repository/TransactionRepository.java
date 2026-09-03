package com.plantify.transaction.transaction.repository;

import com.plantify.transaction.transaction.domain.Status;
import com.plantify.transaction.transaction.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByOrderId(String orderId);
    List<Transaction> findAllByStatusAndCreatedAtBefore(Status status, LocalDateTime expirationTime);
}
