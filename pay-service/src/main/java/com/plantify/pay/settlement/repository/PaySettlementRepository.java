package com.plantify.pay.settlement.repository;

import com.plantify.pay.settlement.domain.PaySettlement;
import com.plantify.pay.ledger.domain.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaySettlementRepository extends JpaRepository<PaySettlement, Long> {

    Optional<PaySettlement> findByPayUserId(Long userId);
    Page<PaySettlement> findByPayUserId(Long userId, Pageable pageable);
    Page<PaySettlement> findByStatusAndPayUserId(Status status, Long userId, Pageable pageable);
    Optional<PaySettlement> findByOrderId(String orderId);
    @Query("SELECT SUM(ps.amount) FROM PaySettlement ps " +
            "JOIN ps.pay p " +
            "WHERE p.userId = :userId " +
            "AND ps.status = 'PAYMENT' " +
            "AND EXTRACT(MONTH FROM ps.createdAt) = :month " +
            "AND EXTRACT(YEAR FROM ps.createdAt) = :year")
    Long getTotalAmountByUserIdAndMonth(@Param("userId") Long userId,
                                        @Param("month") int month,
                                        @Param("year") int year);

}