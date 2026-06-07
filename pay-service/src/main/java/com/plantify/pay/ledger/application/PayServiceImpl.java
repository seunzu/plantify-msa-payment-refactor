package com.plantify.pay.ledger.application;

import com.plantify.pay.account.dto.AccountRequest;
import com.plantify.pay.ledger.dto.PayRequest;
import com.plantify.pay.ledger.dto.PayResponse;
import com.plantify.pay.ledger.domain.Pay;
import com.plantify.pay.point.domain.Point;
import com.plantify.pay.global.exception.ApplicationException;
import com.plantify.pay.global.exception.errorcode.AccountErrorCode;
import com.plantify.pay.global.exception.errorcode.PayErrorCode;
import com.plantify.pay.global.util.UserInfoProvider;
import com.plantify.pay.account.repository.AccountRepository;
import com.plantify.pay.ledger.repository.PayRepository;
import com.plantify.pay.point.repository.PointRepository;
import com.plantify.pay.account.application.AccountService;
import com.plantify.pay.ledger.application.ledger.LedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayServiceImpl implements PayService {

    private final UserInfoProvider userInfoProvider;
    private final AccountService accountService;
    private final LedgerService ledgerService;
    private final AccountRepository accountRepository;
    private final PayRepository payRepository;
    private final PointRepository pointRepository;

    @Override
    @Transactional(readOnly = true)
    public PayResponse getPay() {
        Long userId = userInfoProvider.getUserInfo().userId();
        Pay pay = payRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException(PayErrorCode.PAY_NOT_FOUND));
        return PayResponse.from(pay);
    }

    @Override
    @Transactional
    public PayResponse createPay(AccountRequest request) {
        Long userId = userInfoProvider.getUserInfo().userId();
        if (payRepository.existsByUserId(userId)) {
            throw new ApplicationException(PayErrorCode.PAY_ALREADY_EXISTS);
        }

        Pay pay = new Pay();
        pay.init(userId);
        payRepository.save(pay);

        Point point = new Point();
        point.init(userId);
        pointRepository.save(point);

        accountService.createAccount(request);

        return PayResponse.from(pay);
    }

    @Override
    @Transactional
    public PayResponse balanceRechargePay(PayRequest request) {
        Long userId = userInfoProvider.getUserInfo().userId();
        if (!accountRepository.existsByAccountId(request.accountId())) {
            throw new ApplicationException(AccountErrorCode.ACCOUNT_NOT_FOUND);
        }

        Pay updatedPay = rechargeBalance(userId, request.balance());
        return PayResponse.from(updatedPay);
    }

    @Override
    @Transactional
    public Pay rechargeBalance(Long userId, Long amount) {
        ledgerService.credit(userId, amount, 0L);
        return payRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException(PayErrorCode.PAY_NOT_FOUND));
    }
}
