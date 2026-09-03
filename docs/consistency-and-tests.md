# 검증과 테스트

이 문서는 결제 오케스트레이션 구조 변경에서 확인해야 할 정합성 관점, k6 시나리오, 저장소에 기록한 실험 결과와 해석을 정리

현재 결과는 실제 운영 수준의 분산 트랜잭션 보장 결과가 아니라, mock auth와 로컬 인프라에서 중복 결제, 동시 차감, 결제 승인 후처리 경로를 검증한 결과. 따라서 처리량 수치를 결제 시스템 전체 성능 개선율로 해석하지 않음

## 환경

- Payment 서버: `payment-service`
- Pay 서버: `pay-service`
- Transaction 서버: `transaction-service`
- Auth 대체 서버: WireMock `mock-auth`
- 부하 도구: k6 v0.53.0
- 실행 환경: Docker Compose
- 저장소: pay/payment/transaction-service H2 in-memory
- 인프라: Redis 3 nodes RedLock, Kafka `payment-approved` topic
- 실험용 토큰: `test-token`, 다중 사용자 시나리오용 `test-token-1`~`test-token-5`

## 검증 관점

- 동일 `orderId`로 결제 진입이 중복 생성되지 않는가
- 동일 사용자의 동시 결제 요청에서 잔액이 음수가 되지 않는가
- Ledger debit 요청이 재시도되더라도 같은 `transactionId`로 중복 차감되지 않는가
- Pay debit 이후 Transaction confirm이 실패하면 Pay credit 보상을 시도하는가
- 결제 승인 이후 포인트 적립 후처리가 Kafka `PaymentApproved` 이벤트로 분리되는가
- Kafka 후처리 실패를 결제 승인 자체의 성공/실패와 혼동하지 않는가

## 정합성 전략

핵심 키:

| 키 | 목적 |
| --- | --- |
| `orderId` | 결제 진입 중복 방지용 비즈니스 키 |
| `transactionId` | Ledger debit 재시도/중복 차감 방지용 멱등 키 |
| `userId` | 잔액이라는 공유 자원 보호용 락 키 |

v3 검증 기준:

| 관점 | 전략 |
| --- | --- |
| 중복 결제 진입 | `Transaction.orderId`, `Payment.orderId` unique constraint |
| Payment-Transaction 연결 | `Payment.transactionId` unique constraint |
| Ledger 중복 차감 | `PaySettlement.transactionId` exists check + unique constraint |
| 동일 사용자 동시 결제 | `ledger:{userId}` Redis 분산락 |
| 결제 승인 후처리 이벤트 | Kafka `PaymentApproved` |

## 시나리오 A: 동일 주문 중복 진입

```bash
docker compose --profile test run --rm k6 run /scripts/duplicate-transaction.js
```

동일한 `orderId`로 여러 결제 진입 요청을 동시에 보냈을 때 하나의 Transaction/Payment만 생성되는지 확인

| 항목 | 결과 |
| --- | --- |
| 입력 | 5 VUs, 동일 주문 |
| 기대 결과 | 1건 성공, 나머지 중복 요청 거부 |
| 측정 결과 | 1 x 200, 4 x 409 |
| 상태 | PASS |

## 시나리오 B: 동일 사용자 동시 차감

```bash
docker compose restart pay-service
docker compose --profile test run --rm k6 run /scripts/concurrent-payment-same-user.js
```

동일 사용자의 잔액 500,000원에서 200,000원 결제를 5건 동시에 요청했을 때 잔액 변경이 직렬화되는지 확인

| 항목 | 결과 |
| --- | --- |
| 입력 | 잔액 500,000에서 5 x 200,000 요청 |
| 기대 결과 | 2건 성공, 3건 잔액 부족 |
| 측정 결과 | 2 x 200, 3 x 402, 500 없음 |
| 상태 | PASS |

## 시나리오 C: 서로 다른 사용자 결제

```bash
docker compose --profile test run --rm k6 run /scripts/multi-user-payment.js
```

서로 다른 사용자의 결제 요청이 사용자별 Ledger 락을 기준으로 독립적으로 처리되는지 확인

| 항목 | 결과 |
| --- | --- |
| 입력 | userId 1~5 각 1건 |
| 기대 결과 | 사용자별 결제 승인 |
| 측정 결과 | initiate 5건, approve 5건 성공 |
| 상태 | PASS |

## 시나리오 D: 결제 승인 후처리

결제 승인 이후 Kafka `PaymentApproved` 이벤트가 발행되고, `pay-service`가 이벤트를 소비해 포인트 적립을 수행하는지 확인

Kafka 후처리는 자동 assertion이 아니라 서비스 로그를 통한 수동 확인 결과

```text
PaymentApproved 이벤트 발행 성공. topic=payment-approved
PaymentApprovedEvent 수신. paymentId=15, transactionId=134
포인트 적립. transactionId=134, amount=10000, rewardPoints=50
```

| 항목 | 결과 |
| --- | --- |
| 입력 | 결제 승인 완료 |
| 기대 결과 | `PaymentApproved` 발행, 소비, 포인트 적립 |
| 측정 결과 | 서비스 로그로 발행/소비/적립 확인 |
| 상태 | PASS |

## 보상 처리

```text
Payment Orchestrator
  -> Pay debit 성공
  -> Transaction confirm 실패
  -> Pay credit 보상 시도
```

Transaction confirm 실패 시 debit된 잔액과 포인트 복원을 시도하는 경로

이 보상 로직은 오케스트레이션 사가의 최소 구현. Kafka는 결제 승인 이후 포인트 적립, 알림 같은 비동기 후처리 이벤트를 분리하기 위해 사용

주의할 점:

- `credit` 또는 Transaction `FAILED` 전이가 실패하면 로그를 남기고 수동 처리 대상으로 분류
- `processPayment()`는 `@Transactional` 범위에서 실패 처리 후 `ApplicationException`을 다시 던짐
- `ApplicationException`은 RuntimeException이므로 현재 구조에서는 Payment FAILED 저장이 롤백될 수 있음
- 실패 상태를 반드시 남기려면 별도 트랜잭션 또는 실패 처리 전용 유스케이스로 분리하는 것이 안전

## 해석

- `orderId` unique constraint로 동일 주문의 중복 결제 진입을 방어
- `transactionId` 기반 정산 기록 확인으로 같은 결제 실행의 중복 차감을 방어
- `ledger:{userId}` Redis 분산락으로 동일 사용자 잔액 변경을 직렬화
- Kafka `PaymentApproved`는 결제 승인 이후 포인트 적립 후처리 경로로 동작
- 현재 보상 로직은 오케스트레이션 사가의 최소 구현이며 `credit` 멱등성, 보상 실패 재처리, 실패 상태 저장 분리는 추가 검증이 필요
- Payment 승인 저장 후 Kafka 발행 실패까지 보장하려면 Transactional Outbox 또는 DLQ 기반 재처리 구조가 필요
- 결제 대기 만료 스케줄러는 다중 Pod 환경에서 중복 실행 제어가 필요
- 현재 Ledger는 잔액 상태 중심이며 append-only Ledger entry 모델까지 구현한 것은 아님
