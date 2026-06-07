package com.plantify.pay.account.dto;

import com.plantify.pay.account.domain.Account;
import com.plantify.pay.account.domain.AccountStatus;
import com.plantify.pay.account.domain.BankName;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AccountRequest(
        @Pattern(regexp = "\\d{10,14}", message = "계좌번호는 10~14자리 입니다.")
        String accountNum,
        BankName bankName
) {

    public Account toEntity() {
        return Account.builder()
                .accountNum(accountNum)
                .bankName(bankName)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
    }
}