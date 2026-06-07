package com.plantify.payment.global.exception.errorcode;

import com.plantify.payment.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RefundErrorCode implements ErrorCode {

    REFUND_NOT_FOUND("P201", "환불 요청을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_ACCESS("P202", "접근할 권한이 없습니다.", HttpStatus.FORBIDDEN),
    INVALID_REFUND_STATUS("P203", "유효하지 않은 환불 상태입니다.", HttpStatus.BAD_REQUEST),
    REFUND_ALREADY_PROCESSED("P204", "이미 처리된 환불 요청입니다.", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("P205", "환불 처리 중 서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
