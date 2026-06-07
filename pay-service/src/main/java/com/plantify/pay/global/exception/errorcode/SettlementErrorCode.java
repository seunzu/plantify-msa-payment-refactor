package com.plantify.pay.global.exception.errorcode;

import com.plantify.pay.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SettlementErrorCode implements ErrorCode {

    UNAUTHORIZED_ACCESS("S001", "접근 권한이 없습니다.", HttpStatus.UNAUTHORIZED),
    PAY_SETTLEMENT_NOT_FOUND("S002", "결제 정산 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_TRANSACTION_TYPE("S003", "유효하지 않은 거래 유형입니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
