package com.plantify.pay.kafka;

import com.plantify.pay.ledger.dto.process.TransactionStatusMessage;
import com.plantify.pay.ledger.application.event.PayTransactionStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayConsumer {

    private final PayTransactionStatusService payTransactionStatusService;

    @KafkaListener(
            topics = "${spring.kafka.topic.transaction-status}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"

    )
    public void handleTransactionStatus(TransactionStatusMessage message) {
        log.info("Received TransactionStatusMessage: {}", message);
        try {
            switch (message.status()) {
                case PAYMENT -> payTransactionStatusService.processSuccessfulTransaction(message);
                case FAILED -> payTransactionStatusService.processFailedTransaction(message);
                default -> log.warn("Unknown status: {}", message.status());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}

