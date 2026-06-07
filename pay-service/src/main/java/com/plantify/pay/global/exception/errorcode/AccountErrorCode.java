package com.plantify.pay.global.exception.errorcode;

import com.plantify.pay.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AccountErrorCode implements ErrorCode {

    ACCOUNT_NOT_FOUND("AC001", "계좌를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_ACCOUNT_STATUS("AC002", "유효하지 않은 계좌 상태입니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_ACCESS("AC003", "계좌에 대한 접근 권한이 없습니다.", HttpStatus.UNAUTHORIZED),
    DUPLICATE_ACCOUNT("AC004", "이미 존재하는 계좌입니다.", HttpStatus.CONFLICT),
    MISSING_ACCOUNT("AC005", "페이를 생성하려면 계좌가 필요합니다.", HttpStatus.BAD_REQUEST),
    LEAST_ONE_ACCOUNT("AC006", "계좌는 최소 한 개 이상이어야 합니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
