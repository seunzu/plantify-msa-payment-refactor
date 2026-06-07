package com.plantify.transaction.kafka;

import com.plantify.transaction.transaction.dto.TransactionStatusMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionProvider {

    private final KafkaTemplate<String, TransactionStatusMessage> kafkaTemplate;

    @Value("${spring.kafka.topic.transaction-status}")
    private String kafkaTopic;

    private static final int MAX_RETRY = 3;

    public void sendTransactionStatusMessage(TransactionStatusMessage message) {
        sendWithRetry(message, 0);
    }

    private void sendWithRetry(TransactionStatusMessage message, int attempt) {
        kafkaTemplate.send(kafkaTopic, message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Kafka 발행 성공. transactionId={}, status={}, topic={}, offset={}",
                                message.transactionId(),
                                message.status(),
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().offset());
                        return;
                    }

                    // 발행 실패 — 재시도
                    if (attempt < MAX_RETRY) {
                        long backoffMs = (long) Math.pow(2, attempt) * 1000; // 1s → 2s → 4s
                        log.warn("Kafka 발행 실패. 재시도 {}/{}. transactionId={}, backoff={}ms, error={}",
                                attempt + 1, MAX_RETRY,
                                message.transactionId(),
                                backoffMs,
                                ex.getMessage());

                        try {
                            Thread.sleep(backoffMs);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }

                        sendWithRetry(message, attempt + 1);
                    } else {
                        // 재시도 초과 — DLQ 도입 전까지 로그로 기록
                        log.error("Kafka 발행 최종 실패. DLQ 처리 필요. transactionId={}, status={}, error={}",
                                message.transactionId(),
                                message.status(),
                                ex.getMessage());
                    }
                });
    }
}