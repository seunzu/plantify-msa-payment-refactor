# API 응답 컨벤션

## 성공 응답

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {}
}
```

## 실패 응답

```json
{
  "success": false,
  "code": "P005",
  "message": "중복된 결제 시도가 감지되었습니다.",
  "data": null
}
```

## 필드 설명

| 필드 | 의미 |
| --- | --- |
| `success` | 애플리케이션 수준 요청 처리 성공 여부 |
| `code` | 클라이언트와 서비스 분기에 사용할 안정적인 코드 |
| `message` | 사람이 읽을 수 있는 메시지 |
| `data` | 성공 응답 본문. 실패 시 `null` |

## 에러 코드 접두사

| 접두사 | 범위 |
| --- | --- |
| `Cxxx` | 공통 요청/서버 오류 |
| `Axxx` | 인증/토큰 오류 |
| `Fxxx` | Feign/외부 서비스 호출 오류 |
| `Pxxx` | Payment 결제/환불/취소 오류 |
| `Yxxx` | Pay/Ledger 오류 |
| `ACxxx` | 계좌 오류 |
| `PTxxx` | 포인트 오류 |
| `Sxxx` | 정산 오류 |
| `Txxx` | Transaction 상태 전이 오류 |

## 예외 흐름

```text
Service
  -> ApplicationException(ErrorCode)
  -> GlobalExceptionHandler
  -> ResponseEntity<ApiResponse.fail(...)>
```

Spring MVC 공통 예외도 동일 응답 포맷으로 매핑

- 유효성 검사 실패
- 잘못된 JSON 본문
- 지원하지 않는 HTTP 메서드
- 요청 파라미터/헤더 누락
- 예상치 못한 서버 예외
