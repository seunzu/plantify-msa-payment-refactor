package com.plantify.pay.global.exception.errorcode;

import com.plantify.pay.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PayErrorCode implements ErrorCode {

    UNAUTHORIZED_ACCESS("Y001", "접근 권한이 없습니다.", HttpStatus.UNAUTHORIZED),
    PAY_NOT_FOUND("Y002", "페이를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PAY_ALREADY_EXISTS("Y003", "이미 생성된 페이가 존재합니다.", HttpStatus.CONFLICT),
    INSUFFICIENT_BALANCE("Y004", "잔액이 부족합니다.", HttpStatus.BAD_REQUEST),
    INVALID_PAY_INPUT("Y005", "충전 금액은 0보다 커야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_CHARGE_UNIT("Y006", "충전 금액은 10,000원 단위로만 가능합니다.", HttpStatus.BAD_REQUEST),
    INVALID_REFUND_AMOUNT("Y007", "환불 금액이 결제 금액을 초과할 수 없습니다.", HttpStatus.BAD_REQUEST),
    CONCURRENT_UPDATE("Y008", "동시에 처리 중인 요청이 있습니다.", HttpStatus.CONFLICT),
    DUPLICATE_TRANSACTION("Y009", "이미 진행 중인 결제가 존재합니다.", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
