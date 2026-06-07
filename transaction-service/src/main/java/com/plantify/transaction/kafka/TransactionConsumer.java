package com.plantify.transaction.kafka;

import com.plantify.transaction.transaction.dto.TransactionStatusMessage;
import com.plantify.transaction.transaction.application.TransactionStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionConsumer {

    private final TransactionStatusService transactionStatusService;

    @KafkaListener(
            topics = "${spring.kafka.topic.transaction-status}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleTransactionStatus(TransactionStatusMessage message) {
        try {
            switch (message.status()) {
                case PAYMENT, CANCELLATION, REFUND -> transactionStatusService.processSuccessfulTransaction(message);
                case FAILED -> transactionStatusService.processFailedTransaction(message);
                default -> log.warn("Unknown status: {}", message.status());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}