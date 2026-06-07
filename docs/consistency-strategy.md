# 정합성 전략

## 목표

결제 시스템에서 중요한 것은 단순 처리량보다 금전 상태가 잘못되지 않는 것

정합성 검증 대상:

- 동일 사용자 동시 결제 시 잔액 음수 방지
- 동일 `orderId` 중복 결제 진입 시 Transaction/Payment 중복 생성 방지
- 동일 `transactionId` Ledger 요청 재시도 시 원장 중복 차감 방지
- 결제/환불/취소 상태 전이의 유효성 보장
- Kafka 이벤트 중복 수신 시 후처리 중복 방지
- 부분 실패 시 보상 트랜잭션 필요 지점 식별

---

## v3 전략

| 관심사 | 전략 |
| --- | --- |
| 중복 결제 진입 | `Transaction.orderId`, `Payment.transactionId` unique constraint |
| 원장 동시성 | `ledger:{userId}` Redis 분산락 |
| 잔액 변경 책임 | `pay-service` Ledger로 집중 |
| 결제 흐름 조율 | `payment-service` Orchestrator |
| 거래 최종 상태 | `transaction-service` 상태 전이 |
| 후처리 이벤트 | Kafka `PaymentApproved` message |

---

## 구현된 보상 트랜잭션

```text
Payment -> Pay debit 성공 -> Transaction confirm 실패 -> Pay credit (잔액/포인트 복원)
```

`payment-service`의 오케스트레이터(`PaymentOrchestratorImpl`)에서 Transaction confirm 실패 시 `payServiceClient.credit()`을 호출해 debit된 잔액과 포인트를 복원.
credit 자체도 실패하면 에러 로그를 남기고 수동 처리 대상으로 분류

---

## 고도화 대상

### 환불/복원 멱등성

debit은 `transactionId` 기반 중복 방지 방향이 보이지만, refund/credit도 별도 멱등 키가 필요

### Outbox / DLQ

Payment 승인 저장 후 Kafka 발행이 실패하면 이벤트 유실 가능성이 있음

고도화 방향:

- Transactional Outbox
- Kafka DLQ
- 이벤트 재발행 배치
- consumer idempotency table

### Ledger entry

현재는 잔액 상태 중심. 금전 원장이라는 설득력을 높이려면 변경 이력을 append-only로 남기는 Ledger entry 테이블이 있으면 좋음
