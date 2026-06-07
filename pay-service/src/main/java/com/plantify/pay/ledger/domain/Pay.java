package com.plantify.pay.ledger.domain;

import com.plantify.pay.global.exception.ApplicationException;
import com.plantify.pay.global.exception.errorcode.PayErrorCode;
import com.plantify.pay.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Pay extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long payId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long payNum;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private Long balance;

    @Enumerated(EnumType.STRING)
    @Column
    private Status status;

    public void init(Long userId) {
        this.userId = userId;
        this.payNum = (long) (Math.random() * Math.pow(10, 16));
        this.expiryDate = LocalDateTime.now().plusYears(5);
        this.balance = 0L;
    }

    public Pay updatedBalance(long amount){
        this.balance += amount;
        return this;
    }

    public Pay validateAmount(long amount){
        if (amount <= 0) {
            throw new ApplicationException(PayErrorCode.INVALID_PAY_INPUT);
        }
        if (amount % 10_000 != 0) {
            throw new ApplicationException(PayErrorCode.INVALID_CHARGE_UNIT);
        }
        return this;
    }

    public Pay validatePay(long amount){
        if (this.balance < amount) {
            throw new ApplicationException(PayErrorCode.INSUFFICIENT_BALANCE);
        }
        return this;
    }

    public Pay success(long amount) {
        this.status = Status.APPROVED;
        this.balance -= amount;
        return this;
    }

    public Pay fail(){
        this.status = Status.FAILED;
        return this;
    }
}
