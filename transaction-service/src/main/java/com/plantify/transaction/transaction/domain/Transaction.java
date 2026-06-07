package com.plantify.transaction.transaction.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long transactionId;

    @Column(nullable = false)
    private Long userId;

    @Column
    private Long paymentId;

    @Column(nullable = false, unique = true)
    private String orderId;

    @Column(nullable = false)
    private String orderName;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private Long sellerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column
    private String reason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private String redirectUri;

    public Transaction updateStatus(Status status) {
        this.status = status;
        return this;
    }

    public void updatePaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Transaction updateReason(String reason) {
        this.reason = reason;
        return this;
    }
}
