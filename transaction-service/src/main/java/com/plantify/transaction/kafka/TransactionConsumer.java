package com.plantify.transaction.kafka;

import com.plantify.transaction.transaction.dto.TransactionStatusMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionConsumer {

    @KafkaListener(
            topics = "${spring.kafka.topic.transaction-status}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleTransactionStatus(TransactionStatusMessage message) {
        try {
            switch (message.status()) {
                case COMPLETED -> log.info("결제 완료. transactionId={}, orderId={}",
                        message.transactionId(), message.orderId());
                case REFUNDED -> log.info("환불 완료. transactionId={}, orderId={}",
                        message.transactionId(), message.orderId());
                case CANCELLED -> log.info("취소 완료. transactionId={}, orderId={}",
                        message.transactionId(), message.orderId());
                case FAILED -> log.warn("거래 실패. transactionId={}, orderId={}",
                        message.transactionId(), message.orderId());
                default -> log.warn("알 수 없는 상태. status={}", message.status());
            }
        } catch (Exception e) {
            log.error("Consumer 처리 실패. transactionId={}, error={}",
                    message.transactionId(), e.getMessage());
        }
    }
}