package com.plantify.pay.global.exception.errorcode;

import com.plantify.pay.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FeignErrorCode implements ErrorCode {

    BAD_REQUEST("F001", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("F002", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("F003", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NOT_FOUND("F004", "요청한 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CONFLICT("F005", "충돌이 발생했습니다.", HttpStatus.CONFLICT),
    INTERNAL_SERVER_ERROR("F006", "외부 서비스 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    public static FeignErrorCode of(int status) {
        for (FeignErrorCode code : values()) {
            if (code.httpStatus.value() == status) {
                return code;
            }
        }
        return INTERNAL_SERVER_ERROR;
    }
}
