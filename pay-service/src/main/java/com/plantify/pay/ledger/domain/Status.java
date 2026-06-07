package com.plantify.pay.ledger.domain;

import java.io.Serializable;

public enum Status implements Serializable {

    // Pay 잔액 상태
    CHARGE,     // 충전
    FAILED,     // 처리 실패

    // PaySettlement 상태
    APPROVED,   // 결제 완료
    REFUNDED,   // 환불 완료
    CANCELLED,   // 취소 완료
    REWARDED
}