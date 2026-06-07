package com.plantify.transaction.global.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    String getCode();
    HttpStatus getHttpStatus();
    String getMessage();
}
