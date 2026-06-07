package com.plantify.payment.global.exception.errorcode;

import com.plantify.payment.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

    UNAUTHORIZED_ACCESS("P001", "접근 권한이 없습니다.", HttpStatus.UNAUTHORIZED),
    PAYMENT_FAILED("P002", "결제 처리에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_PAYMENT_METHOD("P003", "유효하지 않은 결제 수단입니다.", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_BALANCE("P004", "잔액이 부족합니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_PAYMENT("P005", "중복된 결제 시도가 감지되었습니다.", HttpStatus.CONFLICT),
    PAYMENT_NOT_FOUND("P006", "해당 결제를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PAYMENT_AMOUNT_MISMATCH("P007", "결제 금액이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_CANCELLED("P008", "이미 취소된 결제입니다.", HttpStatus.BAD_REQUEST),
    EXPIRED_PAYMENT("P009", "결제가 만료되었습니다.", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_STATUS("P010", "유효하지 않은 결제 상태입니다.", HttpStatus.BAD_REQUEST),
    CONCURRENT_UPDATE("P011", "동시에 처리 중인 요청이 있습니다.", HttpStatus.CONFLICT),
    LEDGER_DEBIT_FAILED("P012", "잔액 차감 처리에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    LEDGER_CREDIT_FAILED("P013", "잔액 복원 처리에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    TRANSACTION_CONFIRM_FAILED("P014", "거래 확정 처리에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
