# Architecture

## Goal

자체 Pay 기반 결제 시스템에서 결제 흐름 조율 책임과 금전 원장 변경 책임을 분리하고, MSA 환경에서 중복 요청과 동일 사용자 동시 결제를 제어

## Baseline: v2

```text
Client
  -> pay-service
      -> transaction-service
      -> payment-service
```

v2에서는 `pay-service`가 결제 진입점, 오케스트레이터, Ledger 책임을 함께 담당

실제 `main` 브랜치 기준 결제 확정 흐름:

```mermaid
sequenceDiagram
    participant Client
    participant Pay as pay-service
    participant Tx as transaction-service
    participant Payment as payment-service

    Client->>Pay: 결제 진입
    Pay->>Tx: PENDING 거래 생성
    Tx-->>Pay: transactionId
    Pay-->>Client: payment token

    Client->>Pay: 결제 확정
    Pay->>Tx: Transaction 조회
    Tx-->>Pay: 거래 정보
    Pay->>Pay: Ledger debit
    Pay->>Tx: 성공 전이 요청
    Tx->>Payment: 결제 처리 기록 생성
    Payment-->>Tx: Payment 결과
    Tx->>Tx: PENDING -> PAYMENT
    Tx-->>Pay: 상태 전이 결과
    Pay->>Pay: PaySettlement 저장
    Pay-->>Client: 결제 결과
```

남은 문제:

- Pay가 오케스트레이션과 Ledger를 함께 담당
- 서비스별 락 키가 `ledger:{userId}`, `payment:{userId}`, `transaction:{userId}`로 분산
- Pay debit 이후 Transaction/Payment 처리 실패 시 상태 불일치 가능
- Ledger 차감 자체는 `transactionId` 기반 중복 처리 방어가 부족
- PG 역할과 내부 Pay 역할의 경계가 흐림

## Refactor: v3

```text
Client
  -> payment-service
      -> transaction-service
      -> pay-service
```

v3에서는 `payment-service`가 자체 PG처럼 결제 흐름을 조율하고 `pay-service`는 금전 원장 변경 책임으로 축소. `transaction-service`는 거래 생성과 최종 상태 전이만 담당

서비스 책임:

| Service | Responsibility |
| --- | --- |
| `payment-service` | 결제 진입점, 결제/환불/취소 흐름 조율, Payment 상태 관리, `PaymentApproved` 발행 |
| `pay-service` | Pay 잔액, 포인트, 정산 기록, Ledger 락, 승인 후 포인트 적립 |
| `transaction-service` | Transaction PENDING 생성, COMPLETED/FAILED/REFUNDED/CANCELLED 상태 전이 |

정상 흐름:

```mermaid
sequenceDiagram
    participant Client
    participant Payment as payment-service
    participant Pay as pay-service
    participant Tx as transaction-service
    participant Kafka

    Client->>Payment: 결제 진입
    Payment->>Tx: Transaction PENDING 생성(orderId unique)
    Tx-->>Payment: transactionId
    Payment->>Payment: Payment PENDING 저장(orderId/transactionId unique)
    Payment-->>Client: payment token + redirectUri

    Client->>Payment: 결제 실행
    Payment->>Tx: Transaction PENDING 확인
    Payment->>Pay: debit(transactionId, userId, amount)
    Pay->>Pay: ledger:{userId} lock
    Pay->>Pay: transactionId 처리 여부 확인
    Pay->>Pay: 잔액/포인트 차감 및 settlement 저장
    Pay-->>Payment: debit 완료
    Payment->>Tx: confirm(transactionId, paymentId)
    Tx->>Tx: PENDING -> COMPLETED
    Payment->>Payment: Payment APPROVED 저장
    Payment->>Kafka: PaymentApproved 발행
    Payment-->>Client: 결제 결과
```

실패 및 보상 흐름:

```mermaid
sequenceDiagram
    participant Client
    participant Payment as payment-service
    participant Pay as pay-service
    participant Tx as transaction-service

    Client->>Payment: 결제 실행
    Payment->>Pay: debit
    alt debit 실패
        Pay-->>Payment: 잔액 부족 또는 처리 실패
        Payment->>Payment: Payment FAILED 저장 시도
        Payment->>Tx: fail
    else debit 성공
        Pay-->>Payment: debit 완료
        Payment->>Tx: confirm
        alt confirm 실패
            Tx-->>Payment: 예외
            Payment->>Pay: credit 보상
            Pay-->>Payment: 잔액/포인트 복원
            Payment->>Payment: Payment FAILED 저장 시도
            Payment->>Tx: fail
        else confirm 성공
            Tx-->>Payment: COMPLETED
            Payment->>Payment: Payment APPROVED 저장
        end
    end
    Payment-->>Client: 처리 결과
```

결제 대기 만료:

```text
payment-service Scheduler
  -> 만료 Payment PENDING 조회
  -> transaction-service: PENDING 확인
  -> transaction-service: FAILED 전이
  -> payment-service: Payment FAILED 저장
```

## Auth Assumption

`payment-service`와 `pay-service`는 `auth-service`의 `/v1/auth/validate-token` 호출로 토큰 검증을 위임. `transaction-service`는 내부 거래 상태 서버로 보고 JWT 검증을 두지 않으며, 진입점 서비스가 검증한 사용자 식별자를 내부 요청 DTO로 전달
