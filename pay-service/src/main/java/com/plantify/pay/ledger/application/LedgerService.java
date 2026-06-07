package com.plantify.pay.ledger.application;

import com.plantify.pay.ledger.dto.CreditRequest;
import com.plantify.pay.ledger.dto.DebitRequest;

public interface LedgerService {

    void debit(DebitRequest request);
    void credit(CreditRequest request);
    void reward(Long userId, long rewardPoint);
}
