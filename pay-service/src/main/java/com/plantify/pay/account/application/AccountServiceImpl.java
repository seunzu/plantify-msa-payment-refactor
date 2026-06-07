package com.plantify.pay.account.application;

import com.plantify.pay.account.dto.AccountRequest;
import com.plantify.pay.account.dto.AccountResponse;
import com.plantify.pay.account.domain.Account;
import com.plantify.pay.account.domain.AccountStatus;
import com.plantify.pay.ledger.domain.Pay;
import com.plantify.pay.global.exception.ApplicationException;
import com.plantify.pay.global.exception.errorcode.AccountErrorCode;
import com.plantify.pay.global.exception.errorcode.PayErrorCode;
import com.plantify.pay.account.repository.AccountRepository;
import com.plantify.pay.global.util.UserInfoProvider;
import com.plantify.pay.ledger.repository.PayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final PayRepository payRepository;
    private final UserInfoProvider userInfoProvider;

    @Override
    public List<AccountResponse> getAllAccounts() {
        Long userId = userInfoProvider.getUserInfo().userId();
        return accountRepository.findByPayUserId(userId)
                .stream()
                .map(AccountResponse::from)
                .toList();
    }

    @Override
    public AccountResponse getAccountByAccountId(Long accountId) {
        Long userId = userInfoProvider.getUserInfo().userId();
        Account account = accountRepository.findByAccountIdAndPayUserId(accountId, userId)
                .orElseThrow(() -> new ApplicationException(AccountErrorCode.ACCOUNT_NOT_FOUND));
        return AccountResponse.from(account);
    }

    @Override
    public AccountResponse createAccount(AccountRequest request) {
        if (accountRepository.existsByAccountNum(request.accountNum())) {
            throw new ApplicationException(AccountErrorCode.DUPLICATE_ACCOUNT);
        }

        Long userId = userInfoProvider.getUserInfo().userId();

        Pay pay = payRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException(PayErrorCode.PAY_NOT_FOUND));

        Account account = request.toEntity().linkToPay(pay);;
        accountRepository.save(account);
        return AccountResponse.from(account);
    }

    @Override
    public void deleteAccount(Long accountId) {
        Long userId = userInfoProvider.getUserInfo().userId();

        long accountCount = accountRepository.countByPayUserId(userId);
        if (accountCount <= 1) {
            throw new ApplicationException(AccountErrorCode.LEAST_ONE_ACCOUNT);
        }

        Account account = accountRepository.findByAccountIdAndPayUserId(accountId, userId)
                .orElseThrow(() -> new ApplicationException(AccountErrorCode.ACCOUNT_NOT_FOUND));

        accountRepository.delete(account);
    }
}