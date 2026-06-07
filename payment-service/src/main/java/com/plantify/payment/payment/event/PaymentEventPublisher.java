package com.plantify.payment.payment.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final KafkaTemplate<String, PaymentApprovedEvent> kafkaTemplate;
    private static final int MAX_RETRY = 3;

    @Value("${spring.kafka.topic.payment-approved}")
    private String paymentApprovedTopic;

    public void publishApproved(PaymentApprovedEvent event) {
        publishApproved(event, 0);
    }

    private void publishApproved(PaymentApprovedEvent event, int attempt) {
        kafkaTemplate.send(paymentApprovedTopic, String.valueOf(event.transactionId()), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("PaymentApproved 이벤트 발행 성공. paymentId={}, transactionId={}, topic={}, offset={}",
                                event.paymentId(),
                                event.transactionId(),
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().offset());
                        return;
                    }

                    if (attempt < MAX_RETRY) {
                        long backoffMs = (long) Math.pow(2, attempt) * 1000;
                        log.warn("PaymentApproved 이벤트 발행 실패. 재시도 {}/{}. paymentId={}, transactionId={}, backoff={}ms, error={}",
                                attempt + 1,
                                MAX_RETRY,
                                event.paymentId(),
                                event.transactionId(),
                                backoffMs,
                                ex.getMessage());

                        try {
                            Thread.sleep(backoffMs);
                        } catch (InterruptedException interruptedException) {
                            Thread.currentThread().interrupt();
                            return;
                        }

                        publishApproved(event, attempt + 1);
                        return;
                    }

                    log.error("PaymentApproved 이벤트 발행 최종 실패. Outbox/DLQ 처리 필요. paymentId={}, transactionId={}, error={}",
                            event.paymentId(), event.transactionId(), ex.getMessage());
                });
    }
}
