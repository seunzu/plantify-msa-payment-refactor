package com.plantify.pay.kafka;

import com.plantify.pay.ledger.dto.process.PaymentApprovedEvent;
import com.plantify.pay.ledger.application.PaymentApprovedEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayConsumer {

    private final PaymentApprovedEventService paymentApprovedEventService;

    @KafkaListener(
            topics = "${spring.kafka.topic.payment-approved}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handlePaymentApproved(ConsumerRecord<String, PaymentApprovedEvent> record) {
        PaymentApprovedEvent event = record.value();
        try {
            log.info("PaymentApprovedEvent 수신. paymentId={}, transactionId={}",
                    event.paymentId(), event.transactionId());
            paymentApprovedEventService.processApprovedPayment(event);
        } catch (Exception e) {
            log.error("Consumer 처리 실패. paymentId={}, transactionId={}, error={}",
                    event.paymentId(), event.transactionId(), e.getMessage());
        }
    }
}
