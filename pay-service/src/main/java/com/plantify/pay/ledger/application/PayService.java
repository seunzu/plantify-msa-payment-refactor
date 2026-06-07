package com.plantify.pay.ledger.application;

import com.plantify.pay.account.dto.AccountRequest;
import com.plantify.pay.ledger.dto.PayRequest;
import com.plantify.pay.ledger.dto.PayResponse;
import com.plantify.pay.ledger.domain.Pay;

public interface PayService {

    PayResponse getPay();
    PayResponse createPay(AccountRequest request);
    PayResponse balanceRechargePay(PayRequest request);
    Pay rechargeBalance(Long userId, Long amount);
}
