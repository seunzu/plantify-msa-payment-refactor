package com.plantify.pay.point.application;

import com.plantify.pay.point.domain.Point;
import com.plantify.pay.global.exception.ApplicationException;
import com.plantify.pay.global.exception.errorcode.PointErrorCode;
import com.plantify.pay.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PointCommandServiceImpl implements PointCommandService {

    private final PointRepository pointRepository;

    @Override
    public void addPoints(Long userId, Long newPoints) {

        Point point = pointRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException(PointErrorCode.POINT_NOT_FOUND));

        if (newPoints > 0) {
            point.addPoint(newPoints);
        }

        pointRepository.save(point);
    }

    @Override
    public void usePoints(Long userId, Long pointToUse) {

        Point point = pointRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException(PointErrorCode.POINT_NOT_FOUND));

        point.validatePoint(pointToUse)
                .usePoint(pointToUse);

        pointRepository.save(point);
    }
}
