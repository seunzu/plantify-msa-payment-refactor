package com.plantify.transaction.global.exception.errorcode;

import com.plantify.transaction.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TransactionErrorCode implements ErrorCode {

    // 거래
    TRANSACTION_NOT_FOUND("T001", "거래를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_TRANSACTION_AMOUNT("T002", "거래 금액이 잘못되었습니다.", HttpStatus.BAD_REQUEST),
    TRANSACTION_FAILED("T003", "거래 처리에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DUPLICATE_TRANSACTION("T004", "중복 거래가 감지되었습니다.", HttpStatus.CONFLICT),
    UNAUTHORIZED_ACCESS("T005", "거래에 대한 접근 권한이 없습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TRANSACTION_STATUS("T006", "잘못된 거래 상태입니다.", HttpStatus.BAD_REQUEST),
    SYSTEM_ERROR("T007", "시스템 오류가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 환불
    REFUND_NOT_ALLOWED("T101", "해당 거래는 환불이 허용되지 않습니다.", HttpStatus.BAD_REQUEST),
    REFUND_FAILED("T102", "환불 처리 중 오류가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    REFUND_ALREADY_PROCESSED("T103", "이미 환불된 거래입니다.", HttpStatus.CONFLICT),
    REFUND_AMOUNT_EXCEEDS("T104", "환불 금액이 거래 금액을 초과합니다.", HttpStatus.BAD_REQUEST),

    // 취소
    CANCELLATION_NOT_ALLOWED("T201", "해당 거래는 취소할 수 없습니다.", HttpStatus.BAD_REQUEST),
    CANCELLATION_FAILED("T202", "거래 취소 처리 중 오류가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DUPLICATE_CANCELLATION_REQUEST("T203", "중복된 취소 요청이 감지되었습니다.", HttpStatus.CONFLICT),

    // 동시성 제어
    CONCURRENT_UPDATE("T301", "동시에 처리 중인 요청이 있습니다.", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
