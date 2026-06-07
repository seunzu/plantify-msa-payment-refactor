package com.plantify.pay.ledger.repository;

import com.plantify.pay.ledger.domain.Pay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayRepository extends JpaRepository<Pay, Long> {

    Optional<Pay> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
