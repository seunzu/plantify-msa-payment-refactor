package com.plantify.pay;

import org.springframework.boot.SpringApplication;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, RedissonAutoConfiguration.class, RedissonAutoConfigurationV2.class})
@EnableFeignClients
public class PayApplication {

	public static void main(String[] args) {
		SpringApplication.run(PayApplication.class, args);
	}

	@Bean
	public RecordMessageConverter converter() {
		return new JsonMessageConverter();
	}

}
