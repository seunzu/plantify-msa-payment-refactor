# Experiments

결제 시스템은 auth 리팩토링처럼 단순 처리량 비교보다 정합성 검증이 중요합니다.

이 폴더는 `refactor` 브랜치의 v3 구조를 대상으로 합니다.

## 검증 우선순위

1. 동일 사용자 동시 결제
2. 동일 `orderId` 중복 결제 진입 요청
3. 서로 다른 사용자 동시 결제
4. Pay debit 성공 후 Transaction confirm 실패
5. Kafka 중복 이벤트

## k6

`experiments/k6`에는 HTTP 레벨 동시 요청 스크립트를 둡니다.

| Script | 목적 | 기대 결과 |
| --- | --- | --- |
| `duplicate-transaction.js` | 동일 `orderId` 결제 진입 멱등성 | 1건 성공, 나머지 409 |
| `concurrent-payment-same-user.js` | 동일 사용자 원장 동시성 | 최대 2건 성공, 나머지 402 |
| `multi-user-payment.js` | 서로 다른 사용자 결제 처리 | 모든 결제 승인 |

`multi-user-payment.js`는 WireMock의 `test-token-1`~`test-token-5` 매핑과 pay-service의 userId 1~5 seed 데이터를 사용합니다.

## scenarios

`experiments/scenarios`에는 HTTP 스크립트로 만들기 어려운 장애 주입/부분 실패 시나리오를 문서로 둡니다.
