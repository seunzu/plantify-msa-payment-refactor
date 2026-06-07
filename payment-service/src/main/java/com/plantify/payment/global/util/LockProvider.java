package com.plantify.payment.global.util;

import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class LockProvider {

    private final RedissonClient redissonClient1;
    private final RedissonClient redissonClient2;
    private final RedissonClient redissonClient3;

    public LockProvider(
            @Qualifier("redissonClient1") RedissonClient redissonClient1,
            @Qualifier("redissonClient2") RedissonClient redissonClient2,
            @Qualifier("redissonClient3") RedissonClient redissonClient3) {
        this.redissonClient1 = redissonClient1;
        this.redissonClient2 = redissonClient2;
        this.redissonClient3 = redissonClient3;
    }

    public RLock getPaymentLock(Long userId) {
        String key = "payment:" + userId;
        RLock lock1 = redissonClient1.getLock(key);
        RLock lock2 = redissonClient2.getLock(key);
        RLock lock3 = redissonClient3.getLock(key);
        return new RedissonRedLock(lock1, lock2, lock3);
    }
}