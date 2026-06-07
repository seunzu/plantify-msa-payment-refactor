package com.plantify.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TransactionApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionApplication.class, args);
	}

	@Bean
	public RecordMessageConverter converter() {
		return new JsonMessageConverter();
	}
}
