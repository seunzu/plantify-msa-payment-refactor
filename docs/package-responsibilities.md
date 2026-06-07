# 패키지 책임

## payment-service

| 패키지 | 책임 |
| --- | --- |
| `payment.controller` | 결제/환불/취소 API (`PayProcessController`) |
| `payment.application.payment` | 결제 오케스트레이션 |
| `payment.application.refund` | 환불 오케스트레이션 |
| `payment.application.cancellation` | 취소 오케스트레이션 |
| `payment.domain` | Payment 엔티티와 상태 모델 |
| `payment.dto` | 결제 요청/응답 DTO |
| `payment.repository` | Payment 저장소 |
| `client` | Pay, Transaction, Auth 서비스 연동. Auth는 JWKS가 아니라 `/v1/auth/validate-token` 호출 |
| `global` | 예외, 응답, `JwtFilter` 기반 인증, 공통 설정 |

---

## pay-service

| 패키지 | 책임 |
| --- | --- |
| `ledger` | Pay 잔액 차감/복원/충전, Ledger 락 (`PayLedgerController`) |
| `ledger.application.LedgerService` | 분산락 내 debit/credit/reward 실행 |
| `account` | 계좌 관리 (`AccountController`) |
| `point` | 포인트 조회 및 변경 (`PointController`) |
| `settlement` | 정산 기록 조회 및 변경 (`PaySettlementController`) |
| `settlement.application.PaySettlementDomainService` | settlement 저장/조회/상태 변경 책임 분리 |
| `kafka` | Transaction 상태 이벤트 소비 |
| `client` | Auth, 외부 사용자 서비스 연동 |
| `global` | 예외, 응답, `JwtFilter` 기반 인증, 공통 설정 |

---

## transaction-service

| 패키지 | 책임 |
| --- | --- |
| `transaction.controller` | 내부 거래 API |
| `transaction.application` | 거래 생성, 확정, 환불, 취소, 만료 처리 |
| `transaction.domain` | Transaction 엔티티와 상태 모델 |
| `transaction.dto` | 거래 요청/응답 DTO |
| `transaction.repository` | 거래 상태 조회와 중복 확인 |
| `kafka` | 거래 상태 이벤트 발행 |

`transaction-service`는 내부 서버로 보고 `AuthServiceClient`, `JwtFilter`, `SecurityConfig`를 두지 않음
