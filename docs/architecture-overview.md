# 아키텍처 개요

## 목표

자체 Pay 기반 결제 시스템에서 결제 흐름 조율 책임과 금전 원장 변경 책임을 분리하고, MSA 환경에서 중복 요청과 동일 사용자 동시 결제 요청을 제어

## 브랜치별 구조

### main: v2

```text
Client
  -> pay-service
      -> transaction-service
      -> payment-service
```

`pay-service`가 결제 진입점, 오케스트레이션, Ledger 책임을 함께 가짐. 서비스별로 분산락을 적용했지만, 락 키가 서비스 단위로 나뉘어 동일 자원 보호 기준이 분산됨

### refactor/payment-orchestration-saga: v3

```text
Client
  -> payment-service
      -> pay-service
      -> transaction-service
```

`payment-service`가 오케스트레이터가 되고, `pay-service`는 Ledger 책임으로 축소. 금전 변경은 `userId` 기준 Redis 분산락으로 직렬화하고, 결제 진입 중복은 `orderId` unique constraint로 제어

결제 시작 시 `payment-service`가 `transaction-service`에 Transaction PENDING 생성을 요청하고, 반환된 `transactionId`로 Payment PENDING을 저장. 결제 확정 시에는 Transaction PENDING 상태를 사전 확인한 뒤 Ledger 차감과 Transaction 확정을 순서대로 조율

결제 실패도 `payment-service`가 조율한다. Ledger debit 실패 또는 Transaction confirm 실패 시 Payment를 FAILED로 저장하고, `transaction-service`에 Transaction FAILED 전이를 요청한다. debit 이후 confirm이 실패한 경우에는 Pay credit으로 잔액/포인트를 복원한다.

결제 대기 만료도 `payment-service`가 판단한다. 만료된 Payment PENDING을 찾은 뒤 Transaction이 아직 PENDING이면 `transaction-service`에 FAILED 전이를 요청하고 Payment도 FAILED로 저장한다.

---

## 서비스 역할

| 서비스 | 역할 |
| --- | --- |
| `payment-service` | 결제 진입점, 결제/환불/취소 흐름 조율, Payment 상태 관리 |
| `pay-service` | Pay 잔액, 포인트, 정산 기록, Ledger 락 |
| `transaction-service` | 거래 생성 및 최종 상태 전이 |
| `auth-service` | 이 저장소에는 포함하지 않음. `plantify-msa-auth-refactor`의 `main` 방식처럼 `/v1/auth/validate-token` 네트워크 호출로 토큰 검증 |

---

## 인증 전제

JWKS 로컬 검증 리팩토링을 적용하지 않음

```text
Client
  -> payment/pay service
      -> JwtFilter
          -> auth-service /v1/auth/validate-token

payment/pay service
  -> transaction-service
```

- `payment-service`, `pay-service`: `AuthServiceClient`를 통해 auth 서버에 토큰 검증을 위임한다고 가정
- `transaction-service`는 외부 클라이언트 진입점이 아니라 내부 거래 상태 서버로 보고, JWT 검증과 auth 서버 호출을 두지 않음. 필요한 사용자 식별자는 진입점 서비스가 검증한 뒤 내부 요청 DTO에 포함해 전달

---

## 정합성 전략

| 관심사 | 전략 |
| --- | --- |
| 중복 결제 진입 | `Transaction.orderId`, `Payment.orderId` unique constraint |
| Ledger 재시도 멱등성 | `PaySettlement.transactionId` exists check + unique constraint |
| 동일 사용자 동시 차감 | `ledger:{userId}` Redis 분산락 |
| 상태 전이 | Transaction 상태 검증 후 전이 |
| 비동기 후처리 | `payment-service`가 발행하는 `PaymentApproved` Kafka 이벤트 |
| 결제 대기 만료 | `payment-service` 스케줄러가 Payment/Transaction FAILED 조율 |
| 부분 실패 복원 | Transaction confirm 실패 시 보상 트랜잭션(credit) 실행 후 Transaction FAILED 요청 |
| 고도화 대상 | Outbox, DLQ, credit 멱등성 |
