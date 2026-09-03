package com.plantify.transaction.transaction.repository;

import com.plantify.transaction.transaction.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
