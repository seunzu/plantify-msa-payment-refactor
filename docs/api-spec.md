# API 스펙

Swagger/OpenAPI를 통한 자동 생성 문서도 사용 가능

## 공통

| Method | Path | 설명 |
| --- | --- | --- |
| `GET` | `/` | HealthController. healthcheck 용도로 `OK` 반환 |

---

## payment-service

| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/v1/payments/initiate` | 결제 진입, Transaction PENDING 생성, Payment PENDING 생성 |
| `POST` | `/v1/payments` | 결제 실행 |
| `POST` | `/v1/payments/refunds` | 환불 실행 |
| `POST` | `/v1/payments/cancellations` | 취소 실행 |

---

## pay-service

| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/v1/pay/debit` | Ledger 잔액/포인트 차감 |
| `POST` | `/v1/pay/credit` | Ledger 잔액/포인트 복원 |

---
## transaction-service

| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/v1/transactions` | Payment 오케스트레이터가 호출하는 PENDING 거래 생성 |
| `POST` | `/v1/transactions/confirm` | 결제 확정 |
| `POST` | `/v1/transactions/refunds` | 환불 확정 |
| `POST` | `/v1/transactions/cancellations` | 취소 확정 |

`POST /v1/payments/initiate` 요청에는 `sellerId`, `orderId`, `orderName`, `amount`, `redirectUri`가 포함 

`transactionId`는 `transaction-service`가 PENDING 생성 결과로 반환
