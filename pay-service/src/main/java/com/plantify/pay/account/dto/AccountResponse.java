package com.plantify.pay.account.dto;

import com.plantify.pay.account.domain.Account;
import com.plantify.pay.account.domain.AccountStatus;
import com.plantify.pay.account.domain.BankName;

import java.time.LocalDateTime;

public record AccountResponse(
        Long accountId,
        String accountNum,
        BankName bankName,
        AccountStatus accountStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getAccountId(),
                account.getAccountNum(),
                account.getBankName(),
                account.getAccountStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}