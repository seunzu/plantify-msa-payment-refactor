package com.plantify.payment.payment.domain.entity;

import java.io.Serializable;

public enum Status implements Serializable {

    PENDING, // 처리 시작
    APPROVED, // PG 승인 완료 (결제 성공)
    REFUNDED, // 환불 처리 완료
    CANCELLED, // 취소 처리 완료
    FAILED
}