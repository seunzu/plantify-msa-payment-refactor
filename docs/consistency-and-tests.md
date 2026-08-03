# Consistency and Tests

## Consistency Strategy

결제 시스템에서는 단순 처리량보다 금전 상태가 잘못되지 않는 것이 더 중요

핵심 키:

| Key | Purpose |
| --- | --- |
| `orderId` | 결제 진입 중복 방지용 비즈니스 키 |
| `transactionId` | Ledger debit 재시도/중복 차감 방지용 멱등 키 |
| `userId` | 잔액이라는 공유 자원 보호용 락 키 |

v3 구현 전략:

| Concern | Strategy |
| --- | --- |
| 중복 결제 진입 | `Transaction.orderId`, `Payment.orderId` unique constraint |
| Payment-Transaction 연결 | `Payment.transactionId` unique constraint |
| Ledger 중복 차감 | `PaySettlement.transactionId` exists check + unique constraint |
| 동일 사용자 동시 결제 | `ledger:{userId}` Redis 분산락 |
| 잔액 변경 책임 | `pay-service` Ledger로 집중 |
| 결제 흐름 조율 | `payment-service` Orchestrator |
| 거래 최종 상태 | `transaction-service` 상태 전이 |
| 후처리 이벤트 | Kafka `PaymentApproved` |

## Compensation

```text
Payment -> Pay debit 성공 -> Transaction confirm 실패 -> Pay credit
```

`payment-service`의 `PaymentOrchestratorImpl`은 Transaction confirm 실패 시 `payServiceClient.credit()`을 호출해 debit된 잔액과 포인트 복원을 시도. 이후 Payment FAILED 저장과 Transaction FAILED 전이를 요청

주의할 점:

- `credit` 또는 Transaction `FAILED` 전이가 실패하면 로그를 남기고 수동 처리 대상으로 분류
- `processPayment()`는 `@Transactional` 범위에서 실패 처리 후 `ApplicationException`을 다시 던짐
- `ApplicationException`은 RuntimeException이므로 현재 구조에서는 Payment FAILED 저장이 롤백될 수 있음
- 실패 상태를 반드시 남기려면 별도 트랜잭션 또는 실패 처리 전용 유스케이스로 분리하는 것이 안전

## Experiments

실행 환경:

| Component | Config |
| --- | --- |
| Service DB | pay/payment/transaction-service H2 in-memory |
| Auth | WireMock, `test-token-1`~`test-token-5` -> userId 1~5 |
| Infra | Redis 3 nodes RedLock, Kafka `payment-approved` topic |
| Tool | Docker Compose, k6 v0.53.0 |

결과:

| Scenario | Input | Result | Status |
| --- | --- | --- | --- |
| 동일 `orderId` 중복 진입 | 5 VUs, 동일 주문 | 1 x 200, 4 x 409 | PASS |
| 동일 사용자 동시 차감 | 잔액 500,000에서 5 x 200,000 요청 | 2 x 200, 3 x 402, 500 없음 | PASS |
| 서로 다른 사용자 결제 | userId 1~5 각 1건 | initiate 5건, approve 5건 성공 | PASS |
| `PaymentApproved` 후처리 | 결제 승인 후 로그 확인 | 발행, 소비, 포인트 적립 확인 | PASS |

실행:

```bash
docker compose --profile test run --rm k6 run /scripts/duplicate-transaction.js

docker compose restart pay-service
docker compose --profile test run --rm k6 run /scripts/concurrent-payment-same-user.js

docker compose --profile test run --rm k6 run /scripts/multi-user-payment.js
```

Kafka 후처리는 자동 assertion이 아니라 서비스 로그를 통한 수동 확인 결과

```text
PaymentApproved 이벤트 발행 성공. topic=payment-approved
PaymentApprovedEvent 수신. paymentId=15, transactionId=134
포인트 적립. transactionId=134, amount=10000, rewardPoints=50
```

## Known Limitations

- `credit` 요청은 아직 별도 멱등 키가 부족
- 결제 실패 상태 확정은 정상 결제 트랜잭션과 분리할 필요가 있음
- Payment 승인 저장 후 Kafka 발행 실패에 대비한 Transactional Outbox/DLQ가 없음
- 결제 대기 만료 스케줄러는 다중 Pod 환경에서 중복 실행 제어가 필요
- 현재 Ledger는 잔액 상태 중심이며 append-only Ledger entry 모델은 없음

후속 자동화 대상:

- Pay debit 성공 후 Transaction confirm 실패
- Kafka 중복 이벤트 소비
