package com.plantify.transaction.transaction.domain;

import java.io.Serializable;

public enum Status implements Serializable {

    PENDING, // 처리 시작
    COMPLETED, // 결제 완료 확정(Payment + Ledger 모두 성공)
    REFUNDED, // 환불 완료
    CANCELLED, // 취소 완료
    FAILED // 만료/실패
}