# Test Report

테스트 환경: MacBook, Docker Compose, k6 v0.53.0

## Environment

| Component | Config |
|-----------|--------|
| pay-service | H2 in-memory, userId=1~5 balance=500,000 seed |
| payment-service | H2 in-memory |
| transaction-service | H2 in-memory |
| Auth | WireMock — `test-token` returns userId=1, `test-token-1`~`test-token-5` return userId=1~5 |
| Redis | 3 instances (RedLock via Redisson) |
| Kafka | `payment-approved` topic, pay-service consumer group `pay-service-group` |

---

## Scenario 1 — Duplicate Payment Idempotency

**목적**: 네트워크 재시도나 중복 클릭 상황에서 같은 `orderId`로 결제가 두 번 생성되지 않는지 확인

**스크립트**:
```bash
docker compose --profile test run --rm k6 run /scripts/duplicate-transaction.js
```

k6 options: 5 VUs, 5 iterations shared

**시나리오**: 5개 VU가 동시에 동일한 `orderId`로 `POST /v1/payments/initiate` 호출

**기대 결과**: 1개만 200, 나머지 4개는 409 중복 요청 응답

**메커니즘**: Transaction/Payment 테이블의 유니크 제약 — 동시에 INSERT 시도 시 1개만 성공, 나머지는 중복 요청으로 409

### Result ✅ PASS

```
1 × 200  payment initiated, JWT issued
4 × 409  P005 중복된 결제 시도가 감지되었습니다.

checks: 100% ✓ 5 / ✗ 0
avg response time: 620ms
```

---

## Scenario 2 — Concurrent Balance Deduction (RedLock)

**목적**: 동일 사용자가 동시에 결제할 때 RedLock이 잔액을 직렬화해서 overdraft를 막는지 확인

**스크립트**:
```bash
docker compose restart pay-service  # H2 balance 초기화
docker compose --profile test run --rm k6 run /scripts/concurrent-payment-same-user.js
```

k6 options: 5 VUs, 5 iterations shared

**시나리오**: 5개 VU가 각각 transaction을 생성한 뒤, 동시에 200,000씩 결제 실행. 초기 잔액 500,000

**기대 결과**: 최대 2개 성공(2 × 200,000 = 400,000 ≤ 500,000), 나머지는 402 (잔액 부족). 500 없음

**메커니즘**: pay-service의 RedLock(`ledger:{userId}` 키, 3 Redis 노드)이 동시 debit 요청을 직렬화. 락을 획득한 순서대로 잔액 차감 — 500,000 소진 후 나머지 요청은 잔액 부족 반환

### Result ✅ PASS

```
2 × 200  balance deducted, Payment APPROVED
3 × 402  P012 잔액 차감 처리에 실패했습니다. (insufficient balance)

checks: 100% ✓ 15 / ✗ 0
avg response time: 672ms
```

> pay-service가 H2 in-memory를 사용하므로 테스트 재실행 전 `docker compose restart pay-service`로 잔액을 초기화해야 함

---

## Scenario 3 — Multi User Payment

**목적**: 서로 다른 사용자의 결제 요청은 같은 `ledger:{userId}` 락에 묶이지 않고 독립적으로 처리되는지 확인

**스크립트**:
```bash
docker compose --profile test run --rm k6 run /scripts/multi-user-payment.js
```

k6 options: 5 VUs, 5 iterations shared

**시나리오**: 5개 VU가 `test-token-1`~`test-token-5`로 각각 다른 사용자 결제 진입 후 결제 확정 호출

**기대 결과**: 모든 요청 200. 각 사용자 잔액은 독립적으로 차감

### Result ✅ PASS

```
5 × initiate 200
5 × payment approved 200

checks: 100% ✓ 10 / ✗ 0
http_req_failed: 0%
avg response time: 352ms
```

---

## Scenario 4 — PaymentApproved Kafka Follow-up

**목적**: 결제 승인 후 payment-service가 `PaymentApproved` 이벤트를 발행하고, pay-service가 소비해 포인트 적립 후속 처리를 수행하는지 확인

**검증 방법**:

```bash
docker compose logs --tail=260 pay-service payment-service
```

**기대 결과**:

- payment-service: `PaymentApproved 이벤트 발행 성공`
- pay-service: `PaymentApprovedEvent 수신`
- pay-service: `포인트 적립`

### Result ✅ PASS

```
PaymentApproved 이벤트 발행 성공. topic=payment-approved
PaymentApprovedEvent 수신. paymentId=15, transactionId=134
포인트 적립. transactionId=134, amount=10000, rewardPoints=50
```
