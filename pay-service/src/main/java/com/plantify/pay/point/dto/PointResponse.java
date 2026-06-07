package com.plantify.pay.point.dto;

import com.plantify.pay.point.domain.Point;

public record PointResponse(
        Long userId,
        Long pointBalance
) {
    public static PointResponse from(Point point) {
        return new PointResponse(
                point.getUserId(),
                point.getPointBalance()
        );
    }
}
