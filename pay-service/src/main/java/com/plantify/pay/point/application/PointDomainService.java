package com.plantify.pay.point.application;

public interface PointDomainService {

    void addPoints(Long userId, Long newPoints);
    void usePoints(Long userId, Long pointToUse);
}
