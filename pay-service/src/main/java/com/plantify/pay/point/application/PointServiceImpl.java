package com.plantify.pay.point.application;

import com.plantify.pay.global.exception.ApplicationException;
import com.plantify.pay.global.exception.errorcode.PointErrorCode;
import com.plantify.pay.global.util.UserInfoProvider;
import com.plantify.pay.point.dto.PointResponse;
import com.plantify.pay.point.domain.Point;
import com.plantify.pay.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointRepository pointRepository;
    private final UserInfoProvider userInfoProvider;

    @Override
    public PointResponse getUserPoints() {
        Long userId = userInfoProvider.getUserInfo().userId();

        Point point = pointRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException(PointErrorCode.POINT_NOT_FOUND));

        return PointResponse.from(point);
    }
}
