# plantify-msa-payment-refactor

Spring Boot MSA 환경에서 자체 Pay 결제 흐름의 책임 분리를 비교하는 로컬 실험 레포

- Baseline (`main`, v2): `pay-service`가 결제 진입점, 오케스트레이터, Ledger를 함께 담당
- Refactor target (`refactor/payment-orchestration-saga`, v3): `payment-service`가 결제 흐름을 조율하고 `pay-service`는 Ledger 변경에 집중

## 핵심 변경

- `payment-service`를 자체 PG 성격의 결제 진입점/오케스트레이터로 배치
- `pay-service`를 잔액, 포인트, 정산 Ledger 책임으로 축소
- `transaction-service`를 거래 생성과 최종 상태 전이 책임으로 정리
- `orderId`, `transactionId` 기반으로 중복 결제와 중복 차감 방어
- `ledger:{userId}` Redis lock으로 동일 사용자 잔액 변경 직렬화
- Transaction confirm 실패 시 Pay credit 보상 시도

## 서비스

| 서비스 | 역할 |
| --- | --- |
| `payment-service` | 결제 진입점, 결제/환불/취소 흐름 조율, Payment 상태 관리, `PaymentApproved` 발행 |
| `pay-service` | Pay 잔액, 포인트, 정산 기록, Ledger 락, `PaymentApproved` 소비 후 포인트 적립 |
| `transaction-service` | Transaction PENDING 생성, COMPLETED/FAILED/REFUNDED/CANCELLED 상태 전이 |
| `experiments/k6` | 중복 결제, 동일 사용자 동시 결제, 다중 사용자 결제 검증 스크립트 |

## 로컬 실행

```bash
docker compose up --build redis-1 redis-2 redis-3 zookeeper kafka pay-service transaction-service payment-service
```

`auth-service`는 이 저장소에 포함하지 않음
로컬 실험에서는 `mock-auth`가 auth-service 역할을 대신함

컴파일 확인:

```bash
./gradlew compileJava
```

## 실험 실행

동일 `orderId` 중복 결제 진입 테스트:

```bash
docker compose --profile test run --rm k6 run /scripts/duplicate-transaction.js
```

동일 사용자 동시 결제 테스트:

```bash
docker compose restart pay-service
docker compose --profile test run --rm k6 run /scripts/concurrent-payment-same-user.js
```

서로 다른 사용자 결제 테스트:

```bash
docker compose --profile test run --rm k6 run /scripts/multi-user-payment.js
```

## 문서

- [Architecture](docs/architecture.md)
- [Consistency and Tests](docs/consistency-and-tests.md)
