package com.plantify.payment.client;

import com.plantify.payment.global.AuthUserResponse;

public record AuthValidateResponse(
        boolean success,
        String code,
        String message,
        AuthUserResponse data
) {}
