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

    @Bean(name = "redissonClient1", destroyMethod = "shutdown")
    public RedissonClient redissonClient1() {
        return createClient(redis1);
    }

    @Bean(name = "redissonClient2", destroyMethod = "shutdown")
    public RedissonClient redissonClient2() {
        return createClient(redis2);
    }

    @Bean(name = "redissonClient3", destroyMethod = "shutdown")
    public RedissonClient redissonClient3() {
        return createClient(redis3);
    }

    private RedissonClient createClient(String address) {
        Config config = new Config();
        config.useSingleServer().setAddress(address);
        return Redisson.create(config);
    }
}
