# Refactor Target Sequence: v3

## 정상 흐름

```mermaid
sequenceDiagram
    participant Client
    participant Payment as payment-service
    participant Pay as pay-service
    participant Tx as transaction-service
    participant Kafka

    Client->>Payment: 결제 진입
    Payment->>Tx: Transaction PENDING 생성(userId, sellerId, orderId, amount)
    Tx-->>Payment: transactionId 반환
    Payment->>Payment: Payment PENDING 저장(orderId/transactionId unique)
    Payment-->>Client: payment token + redirectUri

    Client->>Payment: 결제 실행
    Payment->>Tx: Transaction PENDING 조회/확인
    Tx-->>Payment: PENDING Transaction 반환
    Payment->>Pay: debit(transactionId, userId, amount)
    Pay->>Pay: ledger:{userId} lock
    Pay->>Pay: 잔액/포인트 차감 및 settlement 저장
    Pay-->>Payment: debit 완료
    Payment->>Tx: confirm(transactionId, paymentId)
    Tx->>Tx: PENDING -> COMPLETED
    Tx-->>Payment: 확정 결과
    Payment->>Payment: Payment APPROVED 저장
    Payment->>Kafka: PaymentApproved 이벤트 발행
    Payment-->>Client: 결제 결과
```

## 실패 흐름 1: Ledger debit 실패

```mermaid
sequenceDiagram
    participant Client
    participant Payment as payment-service
    participant Pay as pay-service
    participant Tx as transaction-service

    Client->>Payment: 결제 실행
    Payment->>Pay: debit(transactionId, userId, amount)
    Pay-->>Payment: 실패 (잔액 부족/동시성 경합)
    Payment->>Payment: Payment FAILED 저장
    Payment->>Tx: fail(transactionId, paymentId)
    Tx->>Tx: PENDING -> FAILED
    Tx-->>Payment: 실패 확정 결과
    Payment-->>Client: 결제 실패
```

## 실패 흐름 2: Transaction confirm 실패

```mermaid
sequenceDiagram
    participant Client
    participant Payment as payment-service
    participant Pay as pay-service
    participant Tx as transaction-service

    Client->>Payment: 결제 실행
    Payment->>Pay: debit(transactionId, userId, amount)
    Pay->>Pay: 잔액/포인트 차감 완료
    Pay-->>Payment: debit 완료
    Payment->>Tx: confirm(transactionId, paymentId)
    Tx-->>Payment: 실패 (예외)
    Payment->>Pay: credit(userId, amount, point)
    Pay->>Pay: 잔액/포인트 복원
    Pay-->>Payment: credit 완료
    Payment->>Payment: Payment FAILED 저장
    Payment->>Tx: fail(transactionId, paymentId)
    Tx->>Tx: PENDING -> FAILED
    Tx-->>Payment: 실패 확정 결과
    Payment-->>Client: 결제 실패
```

credit 자체가 실패하거나 Transaction FAILED 전이 요청이 실패하면 에러 로그를 남기고 수동 처리 대상으로 분류

## 결제 대기 만료 흐름

```mermaid
sequenceDiagram
    participant Scheduler as payment-service Scheduler
    participant Payment as payment-service
    participant Tx as transaction-service

    Scheduler->>Payment: 만료된 Payment PENDING 조회
    Payment->>Tx: Transaction 상태 조회
    Tx-->>Payment: PENDING
    Payment->>Tx: fail(transactionId, paymentId)
    Tx->>Tx: PENDING -> FAILED
    Tx-->>Payment: 실패 확정 결과
    Payment->>Payment: Payment FAILED 저장
```

## 개선점

- Payment가 오케스트레이터로 결제 흐름을 조율
- Pay는 Ledger 변경 책임에 집중
- Transaction PENDING 생성과 확정 호출은 Payment가 조율
- 금전 변경은 `userId` 기준으로 직렬화
- 결제 진입 중복은 `orderId` 기반 unique constraint로 제어
- Ledger 중복 차감은 `transactionId` 기반 settlement 존재 여부와 unique constraint로 제어
- debit 실패 또는 confirm 실패 시 Payment FAILED와 Transaction FAILED를 함께 조율
- debit 성공 후 confirm 실패 시 보상 트랜잭션(credit)으로 잔액/포인트 복원
- 결제 완료 후속 처리는 PaymentApproved 이벤트를 기준으로 확장
- 결제 대기 만료는 Payment Orchestrator가 판단하고 Transaction FAILED 전이를 요청
