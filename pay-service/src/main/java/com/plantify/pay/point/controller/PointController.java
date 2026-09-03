package com.plantify.pay.point.controller;

import com.plantify.pay.point.dto.PointResponse;
import com.plantify.pay.global.response.ApiResponse;
import com.plantify.pay.point.application.PointQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/pay/points")
public class PointController {

    private final PointQueryService pointQueryService;

    // 자신의 포인트 조회
    @GetMapping
    public ApiResponse<PointResponse> getUserPoints() {
        PointResponse response = pointQueryService.getUserPoints();
        return ApiResponse.ok(response);
    }
}
