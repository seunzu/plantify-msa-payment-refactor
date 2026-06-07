package com.plantify.pay.global.exception.errorcode;

import com.plantify.pay.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PointErrorCode implements ErrorCode {

    POINT_NOT_FOUND("PT001", "포인트 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INSUFFICIENT_POINTS("PT002", "포인트 잔액이 부족합니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_ACCESS("PT003", "포인트 접근 권한이 없습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_POINT_OPERATION("PT004", "유효하지 않은 포인트 작업 요청입니다.", HttpStatus.BAD_REQUEST),
    CONCURRENT_UPDATE("PT005", "포인트 정보 업데이트 중 충돌이 발생했습니다.", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
