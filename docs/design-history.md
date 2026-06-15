# 설계 변화 기록

## 초기 구현

```text
Pay
  = 결제 진입점
  = 오케스트레이터
  = Ledger
```

초기 구현에서는 내부 결제 수단인 Pay를 먼저 만들었기 때문에 Pay가 결제 요청의 진입점이자 흐름 조율자라고 판단

### 문제 인식

- Pay가 결제 흐름 조율과 금전 원장 변경을 동시에 담당
- 결제 성공 판단과 잔액 변경 책임이 섞임
- PG 역할과 내부 Pay 역할의 경계가 흐림

---

## v2: Pay 중심 오케스트레이션 개선

```text
Pay(Orchestrator, Ledger)
  -> Transaction
  -> Payment
```

v2에서는 Pay 중심 구조를 유지하면서 Redis 분산락과 `transactionId` 기반 멱등성을 도입

### 개선한 점

- 중복 요청 문제를 `transactionId`로 인식
- 동일 사용자 잔액 변경에서 동시성 문제가 발생할 수 있음을 명확히 함
- Redisson 기반 락으로 다중 Pod 환경의 경합을 줄이려 함

### 남은 문제

- Pay가 여전히 오케스트레이션과 Ledger를 함께 담당
- Payment, Pay, Transaction 각각의 락 기준이 달라 동일 자원 보호 기준이 분산됨
- 결제 흐름의 최종 성공 판단 위치가 명확하지 않음

---

## v3: Payment 오케스트레이션 + Ledger 집중

```text
Payment(Orchestrator)
  -> Transaction(PENDING 생성/확인)
  -> Pay(Ledger)
  -> Transaction(COMPLETED 확정)
```

v3에서는 Payment를 결제 흐름의 오케스트레이터로 두고, Pay는 금전 원장 변경 책임으로 축소

### 핵심 판단

```text
orderId = 결제 진입 중복 방지용 비즈니스 키
transactionId = Ledger 재시도/중복 차감 방지용 멱등 키
userId = 잔액이라는 공유 자원 보호용 락 키
```

결제 시작 시 `payment-service`가 `transaction-service`에 Transaction PENDING 생성을 요청하고, 반환된 `transactionId`로 Payment PENDING을 저장한다. 결제 확정 시에는 Transaction PENDING 상태를 확인한 뒤 Pay Ledger 차감, Transaction 확정, Payment APPROVED 저장, `PaymentApproved` 이벤트 발행 순서로 진행한다. 결제 실패와 대기 만료 역시 Payment 오케스트레이터가 판단하고 Transaction FAILED 전이를 요청한다.
