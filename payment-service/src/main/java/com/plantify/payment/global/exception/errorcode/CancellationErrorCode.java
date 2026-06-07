package com.plantify.payment.global.exception.errorcode;

import com.plantify.payment.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CancellationErrorCode implements ErrorCode {

    CANCELLATION_NOT_FOUND("P101", "취소 요청을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_CANCELLATION_STATUS("P102", "유효하지 않은 취소 상태입니다.", HttpStatus.BAD_REQUEST),
    CANCELLATION_ALREADY_PROCESSED("P103", "이미 처리된 취소 요청입니다.", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("P104", "취소 처리 중 서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
