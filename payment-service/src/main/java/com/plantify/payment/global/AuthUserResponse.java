package com.plantify.payment.global;

public record AuthUserResponse(
        Long userId,
        String role
) {
}
