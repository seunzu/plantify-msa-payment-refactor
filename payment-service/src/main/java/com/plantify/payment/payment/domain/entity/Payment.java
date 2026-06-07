package com.plantify.payment.payment.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long paymentId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true)
    private Long transactionId;

    @Column(nullable = false, unique = true)
    private String orderId;

    @Column(nullable = false)
    private String orderName;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private Long pointUsed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Method method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column
    private String reason;

    public Payment updateReason(String reason) {
        this.reason = reason;
        return this;
    }

    public Payment updateStatus(Status status) {
        this.status = status;
        return this;
    }

    public Payment updatePointUsed(Long pointUsed) {
        this.pointUsed = pointUsed;
        return this;
    }
}
