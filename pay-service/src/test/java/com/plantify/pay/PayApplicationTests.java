package com.plantify.pay;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = "spring.kafka.listener.auto-startup=false")
class PayApplicationTests {

	@MockBean(name = "redissonClient1")
	private RedissonClient redissonClient1;

	@MockBean(name = "redissonClient2")
	private RedissonClient redissonClient2;

	@MockBean(name = "redissonClient3")
	private RedissonClient redissonClient3;

	@Test
	void contextLoads() {
	}

}
