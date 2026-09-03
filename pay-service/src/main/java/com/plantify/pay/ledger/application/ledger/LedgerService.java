package com.plantify.pay.ledger.application.ledger;

public interface LedgerService {

    void debit(Long userId, long amount, long pointToUse);
    void credit(Long userId, long amount, long point);
    void reward(Long userId, long rewardPoint);
}
