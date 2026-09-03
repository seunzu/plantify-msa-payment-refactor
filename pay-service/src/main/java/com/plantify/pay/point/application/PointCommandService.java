package com.plantify.pay.point.application;

public interface PointCommandService {

    void addPoints(Long userId, Long newPoints);
    void usePoints(Long userId, Long pointToUse);
}
