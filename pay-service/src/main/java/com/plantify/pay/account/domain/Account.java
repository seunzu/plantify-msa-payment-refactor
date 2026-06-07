package com.plantify.pay.account.domain;

import com.plantify.pay.global.domain.BaseEntity;
import com.plantify.pay.ledger.domain.Pay;
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
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payId")
    private Pay pay;

    @Column(nullable = false)
    private String accountNum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BankName bankName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus accountStatus;

    public Account updateStatus(AccountStatus status) {
        this.accountStatus = status;
        return this;
    }

    public Account linkToPay(Pay pay) {
        this.pay = pay;
        return this;
    }
}
