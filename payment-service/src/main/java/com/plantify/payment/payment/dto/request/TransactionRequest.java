package com.plantify.payment.payment.dto.request;

public record TransactionRequest(
        Long userId,
        Long sellerId,
        String orderId,
        String orderName,
        Long amount,
        String redirectUri
) {
    public static TransactionRequest from(PaymentInitRequest request, Long userId) {
        return new TransactionRequest(
                userId,
                request.sellerId(),
                request.orderId(),
                request.orderName(),
                request.amount(),
                request.redirectUri()
        );
    }
}
