package com.plantify.transaction.kafka;

import com.plantify.transaction.transaction.dto.TransactionStatusMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionProvider {

    private final KafkaTemplate<String, TransactionStatusMessage> kafkaTemplate;

    @Value("${spring.kafka.topic.transaction-status}")
    private String kafkaTopic;

    public void sendTransactionStatusMessage(TransactionStatusMessage message) {
        kafkaTemplate.send(kafkaTopic, message);
    }
}