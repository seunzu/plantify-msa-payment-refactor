package com.plantify.pay.settlement.domain;

import com.plantify.pay.global.domain.BaseEntity;
import com.plantify.pay.ledger.domain.Pay;
import com.plantify.pay.ledger.domain.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaySettlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long paySettlementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payId", nullable = false)
    private Pay pay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false, unique = true)
    private Long transactionId;

    @Column
    private String orderId;

    @Column
    private String orderName;

    @Column(nullable = false)
    private Long amount;

    @Column
    private Long pointUsed;

    public PaySettlement updateStatus(Status status) {
        this.status = status;
        return this;
    }
}
