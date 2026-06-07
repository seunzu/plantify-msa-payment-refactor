package com.plantify.pay.client;

import com.plantify.pay.global.AuthUserResponse;

public record AuthValidateResponse(
        boolean success,
        String code,
        String message,
        AuthUserResponse data
) {}
