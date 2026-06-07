package com.plantify.pay.account.application;

import com.plantify.pay.account.dto.AccountRequest;
import com.plantify.pay.account.dto.AccountResponse;

import java.util.List;

public interface AccountService {

    List<AccountResponse> getAllAccounts();
    AccountResponse getAccountByAccountId(Long accountId);
    AccountResponse createAccount(AccountRequest request);
    void deleteAccount(Long accountId);
}