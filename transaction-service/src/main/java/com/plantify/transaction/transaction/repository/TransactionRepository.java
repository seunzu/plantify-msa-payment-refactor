package com.plantify.transaction.transaction.repository;

import com.plantify.transaction.transaction.domain.Status;
import com.plantify.transaction.transaction.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    boolean existsByUserIdAndOrderIdAndStatusIn(Long userId, String orderId, List<Status> statuses);
}
