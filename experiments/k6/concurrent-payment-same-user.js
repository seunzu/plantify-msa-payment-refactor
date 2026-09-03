import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 5,
  iterations: 5,
};

const PAYMENT_BASE_URL = __ENV.PAYMENT_BASE_URL || 'http://localhost:8084';
const AUTH_TOKEN = 'test-token';
const AMOUNT = 200000;

export default function () {
  // 1. 결제 진입: Transaction PENDING 생성
  const initRes = http.post(
    `${PAYMENT_BASE_URL}/v1/payments/initiate`,
    JSON.stringify({
      sellerId: 1,
      orderId: `ORDER-VU${__VU}-${Date.now()}`,
      orderName: 'Concurrent payment test',
      amount: AMOUNT,
      redirectUri: 'http://localhost:3000/result',
    }),
    {
      headers: {
        Authorization: `Bearer ${AUTH_TOKEN}`,
        'Content-Type': 'application/json',
      },
    }
  );

  check(initRes, { 'initiate ok': (r) => r.status === 200 });

  const token = initRes.json('data.token');
  if (!token) return;

  // 2. 결제 실행: 동시에 debit을 요청하고 RedLock으로 잔액 변경을 보호
  const payRes = http.post(
    `${PAYMENT_BASE_URL}/v1/payments`,
    null,
    { headers: { token: token } }
  );

  console.log(`VU${__VU} execute: status=${payRes.status} body=${payRes.body}`);

  check(payRes, {
    'not server error': (r) => r.status < 500,
    'success or debit failed': (r) => r.status === 200 || r.status === 402,
  });
}
