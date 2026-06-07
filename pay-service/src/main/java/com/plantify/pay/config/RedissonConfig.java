package com.plantify.pay.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${redisson.redlock.servers[0]}")
    private String redis1;

    @Value("${redisson.redlock.servers[1]}")
    private String redis2;

    @Value("${redisson.redlock.servers[2]}")
    private String redis3;

    @Bean("redissonClient1")
    public RedissonClient redissonClient1() {
        Config config = new Config();
        config.useSingleServer().setAddress(redis1);
        return Redisson.create(config);
    }

    @Bean("redissonClient2")
    public RedissonClient redissonClient2() {
        Config config = new Config();
        config.useSingleServer().setAddress(redis2);
        return Redisson.create(config);
    }

    @Bean("redissonClient3")
    public RedissonClient redissonClient3() {
        Config config = new Config();
        config.useSingleServer().setAddress(redis3);
        return Redisson.create(config);
    }
}
